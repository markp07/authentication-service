package nl.markpost.demo.authentication.service;

import static nl.markpost.demo.authentication.constant.Constants.ACCESS_TOKEN;
import static nl.markpost.demo.authentication.constant.Constants.DAYS_7;
import static nl.markpost.demo.authentication.constant.Constants.MINUTES_15;
import static nl.markpost.demo.authentication.constant.Constants.PASSKEY_REGISTRATION;
import static nl.markpost.demo.authentication.constant.Constants.REFRESH_TOKEN;
import static nl.markpost.demo.authentication.util.MessageResponseUtil.createMessageResponse;

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
import com.yubico.webauthn.data.UserVerificationRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.api.v1.model.PasskeyInfoDto;
import nl.markpost.demo.authentication.api.v1.model.PublicKeyCredentialCreationOptionsDto;
import nl.markpost.demo.authentication.constant.Messages;
import nl.markpost.demo.authentication.mapper.PasskeyCredentialMapper;
import nl.markpost.demo.authentication.mapper.PasskeyInfoDtoMapper;
import nl.markpost.demo.authentication.mapper.PublicKeyCredentialCreationOptionsDtoMapper;
import nl.markpost.demo.authentication.mapper.StartRegistrationOptionsMapper;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.PasskeyCredentialRepository;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.authentication.util.CookieUtil;
import nl.markpost.demo.authentication.util.RequestUtil;
import nl.markpost.demo.authentication.util.UserUtil;
import nl.markpost.demo.common.exception.InternalServerErrorException;
import nl.markpost.demo.common.exception.NotFoundException;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

//TODO: move controller logic into here
//TODO: add JavaDoc to class and all methods
//TODO: Refactor to remove duplicate code between finishAuthentication and finishUsernamelessAuthentication
@Service
@RequiredArgsConstructor
@Slf4j
public class PasskeyService {

  private final PasskeyCredentialRepository passkeyCredentialRepository;
  private final UserRepository userRepository;
  private final RelyingParty relyingParty;
  private final JwtService jwtService;

  private final PasskeyInfoDtoMapper passkeyInfoDtoMapper;
  private final PublicKeyCredentialCreationOptionsDtoMapper publicKeyCredentialCreationOptionsDtoMapper;
  private final StartRegistrationOptionsMapper startRegistrationOptionsMapper;
  private final PasskeyCredentialMapper passkeyCredentialMapper;

  /**
   * Lists all passkeys for the given user.
   *
   * @param user the user whose passkeys are to be listed
   * @return a list of PasskeyInfoDto representing the user's passkeys
   */
  public List<PasskeyInfoDto> listPasskeys(User user) {
    if (user == null) return List.of();
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

    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    HttpSession session = attrs.getRequest().getSession(true);
    session.setAttribute(PASSKEY_REGISTRATION, options);

    return publicKeyCredentialCreationOptionsDtoMapper.from(options);
  }

  /**
   * Finishes the registration process for a new passkey.
   *
   * @param credential the PublicKeyCredential received from the client
   * @param name       the name of the passkey
   */
  @SneakyThrows
  public void finishRegistration(
      PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential,
      String name) {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    HttpSession session = attrs.getRequest().getSession(true);
    PublicKeyCredentialCreationOptions registrationOptions = (PublicKeyCredentialCreationOptions) session.getAttribute(
        PASSKEY_REGISTRATION);

    User user = UserUtil.getUserFromSecurityContext();
    FinishRegistrationOptions finishOptions = createFinishRegistrationOptions(credential, registrationOptions);
    RegistrationResult result = relyingParty.finishRegistration(finishOptions);

    PasskeyCredential passkey = passkeyCredentialMapper.from(result, user, name);
    passkeyCredentialRepository.save(passkey);
    session.removeAttribute(PASSKEY_REGISTRATION);
  }

  public AssertionRequest startAuthentication(String email) {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("User not found");
    }
    AssertionRequest assertionRequest = relyingParty.startAssertion(
        StartAssertionOptions.builder()
            .username(email)
            .build()
    );
    log.info("[WebAuthn] AssertionRequest allowCredentials: "
        + assertionRequest.getPublicKeyCredentialRequestOptions().getAllowCredentials());
    return assertionRequest;
  }

  @SneakyThrows
  public ResponseEntity<Message> finishAuthentication(String email,
      PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential,
      AssertionRequest assertionRequest) {
    log.info("[WebAuthn] finishAuthentication credentialId: " + credential.getId().getBase64Url());
    AssertionResult result = relyingParty.finishAssertion(
        FinishAssertionOptions.builder()
            .request(assertionRequest)
            .response(credential)
            .build()
    );
    if (result.isSuccess()) {
      User user = userRepository.findByEmail(email);
      if (user == null) {
        throw new UnauthorizedException();
      }

      HttpServletResponse response = RequestUtil.getCurrentResponse();
      String accessToken = jwtService.generateAccessToken(user);
      response.addCookie(CookieUtil.buildCookie(ACCESS_TOKEN, accessToken, MINUTES_15));

      String refreshToken = jwtService.generateRefreshToken(user);
      response.addCookie(CookieUtil.buildCookie(REFRESH_TOKEN, refreshToken, DAYS_7));

      return ResponseEntity.status(HttpStatus.OK)
          .body(createMessageResponse(Messages.LOGIN_SUCCESS));
    } else {
      throw new UnauthorizedException();
    }
  }

  public AssertionRequest startUsernamelessAuthentication() {
    // Start assertion without specifying a username - allows any discoverable credential
    AssertionRequest assertionRequest = relyingParty.startAssertion(
        StartAssertionOptions.builder()
            .userVerification(UserVerificationRequirement.REQUIRED)
            // No username specified - allows discoverable credentials for this RP
            .build()
    );
    log.info("[WebAuthn] Usernameless AssertionRequest started");
    return assertionRequest;
  }

  @SneakyThrows
  public ResponseEntity<Message> finishUsernamelessAuthentication(
      PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential,
      AssertionRequest assertionRequest) {
    log.info("[WebAuthn] finishUsernamelessAuthentication credentialId: " + credential.getId().getBase64Url());

    AssertionResult result = relyingParty.finishAssertion(
        FinishAssertionOptions.builder()
            .request(assertionRequest)
            .response(credential)
            .build()
    );

    if (result.isSuccess()) {
      // Find user by credential ID since we don't have email
      String credentialIdBase64 = credential.getId().getBase64Url();
      PasskeyCredential passkeyCredential = passkeyCredentialRepository.findByCredentialId(credentialIdBase64);

      if (passkeyCredential == null) {
        log.warn("[WebAuthn] No passkey credential found for ID: " + credentialIdBase64);
        throw new UnauthorizedException();
      }

      User user = passkeyCredential.getUser();
      if (user == null) {
        log.warn("[WebAuthn] No user found for passkey credential: " + credentialIdBase64);
        throw new UnauthorizedException();
      }

      HttpServletResponse response = RequestUtil.getCurrentResponse();
      String accessToken = jwtService.generateAccessToken(user);
      response.addCookie(CookieUtil.buildCookie(ACCESS_TOKEN, accessToken, MINUTES_15));

      String refreshToken = jwtService.generateRefreshToken(user);
      response.addCookie(CookieUtil.buildCookie(REFRESH_TOKEN, refreshToken, DAYS_7));

      log.info("[WebAuthn] Usernameless authentication successful for user: " + user.getEmail());
      return ResponseEntity.status(HttpStatus.OK)
          .body(createMessageResponse(Messages.LOGIN_SUCCESS));
    } else {
      log.warn("[WebAuthn] Usernameless authentication failed");
      throw new UnauthorizedException();
    }
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

  private FinishRegistrationOptions createFinishRegistrationOptions(
      PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential,
      PublicKeyCredentialCreationOptions registrationOptions) {
    return FinishRegistrationOptions.builder()
        .request(registrationOptions)
        .response(credential)
        .build();
  }
}
