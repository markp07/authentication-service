package nl.markpost.authentication.service;

import static nl.markpost.authentication.constant.Constants.ACCESS_TOKEN;
import static nl.markpost.authentication.constant.Constants.DAYS_7;
import static nl.markpost.authentication.constant.Constants.MINUTES_15;
import static nl.markpost.authentication.constant.Constants.PASSKEY_ASSERTION_REQUEST;
import static nl.markpost.authentication.constant.Constants.PASSKEY_REGISTRATION;
import static nl.markpost.authentication.constant.Constants.PASSKEY_USERNAMELESS_ASSERTION_REQUEST;
import static nl.markpost.authentication.constant.Constants.REFRESH_TOKEN;
import static nl.markpost.authentication.util.MessageResponseUtil.createMessageResponse;

import tools.jackson.databind.ObjectMapper;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import com.yubico.webauthn.data.UserVerificationRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.api.v1.model.PasskeyInfoDto;
import nl.markpost.authentication.api.v1.model.PublicKeyCredentialCreationOptionsDto;
import nl.markpost.authentication.api.v1.model.PublicKeyCredentialCreationOptionsDtoAuthenticatorSelection;
import nl.markpost.authentication.api.v1.model.PublicKeyCredentialCreationOptionsDtoPubKeyCredParamsInner;
import nl.markpost.authentication.api.v1.model.PublicKeyCredentialCreationOptionsDtoRp;
import nl.markpost.authentication.api.v1.model.PublicKeyCredentialCreationOptionsDtoUser;
import nl.markpost.authentication.api.v1.model.PublicKeyCredentialRequestOptionsDto;
import nl.markpost.authentication.constant.Messages;
import nl.markpost.authentication.exception.InternalServerErrorException;
import nl.markpost.authentication.exception.NotFoundException;
import nl.markpost.authentication.exception.UnauthorizedException;
import nl.markpost.authentication.mapper.PasskeyCredentialMapper;
import nl.markpost.authentication.mapper.PasskeyInfoDtoMapper;
import nl.markpost.authentication.mapper.StartRegistrationOptionsMapper;
import nl.markpost.authentication.model.PasskeyCredential;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.PasskeyCredentialRepository;
import nl.markpost.authentication.util.CookieUtil;
import nl.markpost.authentication.util.RequestUtil;
import nl.markpost.authentication.util.UserUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Service for handling passkey registration and authentication operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasskeyService {

  private final PasskeyCredentialRepository passkeyCredentialRepository;
  private final RelyingParty relyingParty;
  private final JwtService jwtService;
  private final ObjectMapper objectMapper;
  private final UserService userService;

  private final PasskeyInfoDtoMapper passkeyInfoDtoMapper;
  private final StartRegistrationOptionsMapper startRegistrationOptionsMapper;
  private final PasskeyCredentialMapper passkeyCredentialMapper;

  /**
   * Lists all passkeys for the given user.
   *
   * @param user the user whose passkeys are to be listed
   * @return a list of PasskeyInfoDto representing the user's passkeys
   */
  public List<PasskeyInfoDto> listPasskeys(User user) {
    if (user == null) {
      return List.of();
    }
    List<PasskeyCredential> list = passkeyCredentialRepository.findByUserId(user.getId());
    return list
        .stream()
        .map(passkeyInfoDtoMapper::from)
        .toList();
  }

  /**
   * Starts the registration process for a new passkey.
   *
   * @return PublicKeyCredentialCreationOptionsDto containing the registration options
   */
  public PublicKeyCredentialCreationOptionsDto startRegistration() {
    User user = UserUtil.getUserFromSecurityContext();
    ByteArray userIdBytes = UserUtil.getIdAsByteArray(user);

    StartRegistrationOptions startOptions = startRegistrationOptionsMapper.from(userIdBytes, user);

    PublicKeyCredentialCreationOptions options = relyingParty.startRegistration(startOptions);

    HttpSession session = getSession();
    session.setAttribute(PASSKEY_REGISTRATION, options);

    return convertToCreationOptionsDto(options);
  }

  /**
   * Finishes the registration process for a new passkey.
   *
   * @param credentialBody the credential object from the client
   * @param name           the name of the passkey
   */
  @SneakyThrows
  public void finishRegistration(Object credentialBody, String name) {
    PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential =
        convertToAttestationCredential(credentialBody);

    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    HttpSession session = attrs.getRequest().getSession(true);
    PublicKeyCredentialCreationOptions registrationOptions = (PublicKeyCredentialCreationOptions) session.getAttribute(
        PASSKEY_REGISTRATION);

    User user = UserUtil.getUserFromSecurityContext();
    FinishRegistrationOptions finishOptions = FinishRegistrationOptions.builder()
        .request(registrationOptions)
        .response(credential)
        .build();

    RegistrationResult result = relyingParty.finishRegistration(finishOptions);

    PasskeyCredential passkey = passkeyCredentialMapper.from(result, user, name);
    passkey.setId(null);
    passkeyCredentialRepository.save(passkey);
    session.removeAttribute(PASSKEY_REGISTRATION);
  }

  /**
   * Starts the passkey authentication process for a user.
   *
   * @param email the user's email
   * @return PublicKeyCredentialRequestOptionsDto containing the authentication options
   */
  public PublicKeyCredentialRequestOptionsDto startAuthentication(String email) {
    userService.checkIfUserExists(email, false);

    AssertionRequest assertionRequest = relyingParty.startAssertion(
        StartAssertionOptions.builder()
            .username(email)
            .build()
    );

    HttpSession session = getSession();
    session.setAttribute(PASSKEY_ASSERTION_REQUEST, assertionRequest);

    return convertToRequestOptionsDto(assertionRequest.getPublicKeyCredentialRequestOptions());
  }

  /**
   * Finishes the passkey authentication process.
   *
   * @param email          the user's email
   * @param credentialBody the credential object from the client
   * @return ResponseEntity with authentication result message
   */
  @SneakyThrows
  public ResponseEntity<Message> finishAuthentication(String email, Object credentialBody) {
    PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential =
        convertToAssertionCredential(credentialBody);

    HttpSession session = getSession();
    AssertionRequest assertionRequest = (AssertionRequest) session.getAttribute(
        PASSKEY_ASSERTION_REQUEST);

    User user = userService.getUserByEmail(email);
    ResponseEntity<Message> result = completeAssertion(credential, assertionRequest, user);
    session.removeAttribute(PASSKEY_ASSERTION_REQUEST);

    return result;
  }

  /**
   * Starts the usernameless passkey authentication process.
   *
   * @return PublicKeyCredentialRequestOptionsDto containing the authentication options
   */
  public PublicKeyCredentialRequestOptionsDto startUsernamelessAuthentication() {
    AssertionRequest assertionRequest = relyingParty.startAssertion(
        StartAssertionOptions.builder()
            .userVerification(UserVerificationRequirement.REQUIRED)
            .build()
    );

    HttpSession session = getSession();
    session.setAttribute(PASSKEY_USERNAMELESS_ASSERTION_REQUEST, assertionRequest);

    return convertToRequestOptionsDto(assertionRequest.getPublicKeyCredentialRequestOptions());
  }

  /**
   * Finishes the usernameless passkey authentication process.
   *
   * @param credentialBody the credential object from the client
   * @return ResponseEntity with authentication result message
   */
  @SneakyThrows
  public ResponseEntity<Message> finishUsernamelessAuthentication(Object credentialBody) {
    PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential =
        convertToAssertionCredential(credentialBody);

    HttpSession session = getSession();
    AssertionRequest assertionRequest = (AssertionRequest) session.getAttribute(
        PASSKEY_USERNAMELESS_ASSERTION_REQUEST);

    String credentialIdBase64 = credential.getId().getBase64Url();
    PasskeyCredential passkeyCredential = passkeyCredentialRepository.findByCredentialId(
        credentialIdBase64).orElseThrow(
        UnauthorizedException::new);

    User user = passkeyCredential.getUser();
    if (user == null) {
      throw new UnauthorizedException();
    }

    ResponseEntity<Message> result = completeAssertion(credential, assertionRequest, user);
    session.removeAttribute(PASSKEY_USERNAMELESS_ASSERTION_REQUEST);

    return result;
  }

  @Transactional
  public void deletePasskey(String credentialId) {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (!(principal instanceof User user)) {
      throw new InternalServerErrorException();
    }

    PasskeyCredential cred = passkeyCredentialRepository
        .findByCredentialIdAndUserId(credentialId, user.getId())
        .orElseThrow(() -> new NotFoundException("Passkey not found"));

    passkeyCredentialRepository.delete(cred);
  }

  /**
   * Completes the assertion (authentication) process and issues tokens.
   *
   * @param credential       the public key credential
   * @param assertionRequest the assertion request from session
   * @param user             the user to authenticate
   * @return ResponseEntity with success message
   */
  @SneakyThrows
  private ResponseEntity<Message> completeAssertion(
      PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential,
      AssertionRequest assertionRequest,
      User user) {

    AssertionResult result = relyingParty.finishAssertion(
        FinishAssertionOptions.builder()
            .request(assertionRequest)
            .response(credential)
            .build()
    );

    if (!result.isSuccess()) {
      throw new UnauthorizedException();
    }

    issueAuthenticationTokens(user);

    return ResponseEntity.status(HttpStatus.OK)
        .body(createMessageResponse(Messages.LOGIN_SUCCESS));
  }

  /**
   * Issues access and refresh tokens for the authenticated user.
   *
   * @param user the authenticated user
   */
  private void issueAuthenticationTokens(User user) {
    HttpServletResponse response = RequestUtil.getCurrentResponse();

    String accessToken = jwtService.generateAccessToken(user);
    response.addCookie(CookieUtil.buildCookie(ACCESS_TOKEN, accessToken, MINUTES_15));

    String refreshToken = jwtService.generateRefreshToken(user);
    response.addCookie(CookieUtil.buildCookie(REFRESH_TOKEN, refreshToken, DAYS_7));
  }

  /**
   * Converts a generic object to a PublicKeyCredential for attestation (registration).
   *
   * @param credentialBody the credential object
   * @return the converted PublicKeyCredential
   */
  private PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>
  convertToAttestationCredential(Object credentialBody) {
    return objectMapper.convertValue(credentialBody,
        objectMapper.getTypeFactory().constructParametricType(
            PublicKeyCredential.class,
            AuthenticatorAttestationResponse.class,
            ClientRegistrationExtensionOutputs.class));
  }

  /**
   * Converts a generic object to a PublicKeyCredential for assertion (authentication).
   *
   * @param credentialBody the credential object
   * @return the converted PublicKeyCredential
   */
  private PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>
  convertToAssertionCredential(Object credentialBody) {
    return objectMapper.convertValue(credentialBody,
        objectMapper.getTypeFactory().constructParametricType(
            PublicKeyCredential.class,
            AuthenticatorAssertionResponse.class,
            ClientAssertionExtensionOutputs.class));
  }

  /**
   * Gets the current HTTP session.
   *
   * @return the current HTTP session
   */
  private HttpSession getSession() {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    return attrs.getRequest().getSession(true);
  }

  /**
   * Converts PublicKeyCredentialCreationOptions to DTO.
   *
   * @param options the options to convert
   * @return the DTO
   */
  private PublicKeyCredentialCreationOptionsDto convertToCreationOptionsDto(
      PublicKeyCredentialCreationOptions options) {
    PublicKeyCredentialCreationOptionsDto dto = new PublicKeyCredentialCreationOptionsDto();
    dto.setChallenge(options.getChallenge().getBase64Url());
    dto.setRp(objectMapper.convertValue(options.getRp(),
        PublicKeyCredentialCreationOptionsDtoRp.class));
    dto.setUser(objectMapper.convertValue(options.getUser(),
        PublicKeyCredentialCreationOptionsDtoUser.class));
    dto.setPubKeyCredParams(objectMapper.convertValue(options.getPubKeyCredParams(),
        objectMapper.getTypeFactory().constructCollectionType(List.class,
            PublicKeyCredentialCreationOptionsDtoPubKeyCredParamsInner.class)));
    dto.setTimeout(options.getTimeout().orElse(null));
    dto.setAuthenticatorSelection(options.getAuthenticatorSelection().isPresent() ?
        objectMapper.convertValue(options.getAuthenticatorSelection().get(),
            PublicKeyCredentialCreationOptionsDtoAuthenticatorSelection.class)
        : null);
    return dto;
  }

  /**
   * Converts PublicKeyCredentialRequestOptions to DTO.
   *
   * @param options the options to convert
   * @return the DTO
   */
  private PublicKeyCredentialRequestOptionsDto convertToRequestOptionsDto(
      PublicKeyCredentialRequestOptions options) {
    PublicKeyCredentialRequestOptionsDto dto = new PublicKeyCredentialRequestOptionsDto();
    dto.setChallenge(options.getChallenge().getBase64Url());
    dto.setTimeout(options.getTimeout().orElse(null));
    dto.setRpId(options.getRpId());
    if (options.getAllowCredentials().isPresent() && !options.getAllowCredentials().get()
        .isEmpty()) {
      @SuppressWarnings("unchecked")
      List<Object> allowCredentials = objectMapper.convertValue(options.getAllowCredentials().get(),
          List.class);
      dto.setAllowCredentials(allowCredentials);
    }
    if (options.getUserVerification().isPresent()) {
      String uvValue = options.getUserVerification().get().getValue();
      dto.setUserVerification(
          PublicKeyCredentialRequestOptionsDto.UserVerificationEnum.fromValue(uvValue));
    }
    dto.setHints(
        options.getHints() != null && !options.getHints().isEmpty()
            ? options.getHints()
            : null
    );
    return dto;
  }
}
