package nl.markpost.demo.authentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.api.v1.controller.PasskeyApi;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.api.v1.model.PasskeyInfoDto;
import nl.markpost.demo.authentication.api.v1.model.PasskeyLoginFinishRequest;
import nl.markpost.demo.authentication.api.v1.model.PasskeyLoginStartRequest;
import nl.markpost.demo.authentication.api.v1.model.PublicKeyCredentialAssertionDto;
import nl.markpost.demo.authentication.api.v1.model.PublicKeyCredentialCreationOptionsDto;
import nl.markpost.demo.authentication.api.v1.model.PublicKeyCredentialRequestOptionsDto;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.service.PasskeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

//TODO: add JavaDoc to class and all methods
//TODO: move logic to service layer where applicable
@RestController
@RequestMapping("/v1/passkey")
@RequiredArgsConstructor
public class PasskeyController implements PasskeyApi {

  private final PasskeyService passkeyService;

  /**
   * List all passkeys for the authenticated user.
   *
   * @return A ResponseEntity containing a list of PasskeyInfoDto objects.
   */
  @Override
  public ResponseEntity<List<PasskeyInfoDto>> listPasskeys() {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return ResponseEntity.ok(passkeyService.listPasskeys(user));
  }

  /**
   * Start the passkey registration process.
   *
   * @return A ResponseEntity containing the PublicKeyCredentialCreationOptionsDto.
   */
  @Override
  public ResponseEntity<PublicKeyCredentialCreationOptionsDto> startPasskeyRegistration() {
    PublicKeyCredentialCreationOptionsDto options = passkeyService.startRegistration();
    return ResponseEntity.ok(options);
  }

  /**
   * Finish the passkey registration process.
   *
   * @param name The name of the passkey.
   * @param body The request body containing the PublicKeyCredential.
   * @return A ResponseEntity with no content.
   */
  @Override
  public ResponseEntity<Void> finishPasskeyRegistration(String name, Object body) {
    ObjectMapper objectMapper = new ObjectMapper();
    PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential =
        objectMapper.convertValue(body,
            objectMapper.getTypeFactory().constructParametricType(
                PublicKeyCredential.class,
                AuthenticatorAttestationResponse.class,
                ClientRegistrationExtensionOutputs.class));

    passkeyService.finishRegistration(credential, name);
    return ResponseEntity.ok().build();
  }


  @Override
  public ResponseEntity<PublicKeyCredentialRequestOptionsDto> startPasskeyLogin(
      PasskeyLoginStartRequest request) {
    // Get the full AssertionRequest and store it in session
    AssertionRequest assertionRequest = passkeyService.startAuthentication(request.getEmail());
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    HttpSession session = attrs.getRequest().getSession(true);
    session.setAttribute("webauthn_assertion_request", assertionRequest);

    // Return custom DTO that properly excludes null values
    nl.markpost.demo.authentication.dto.PublicKeyCredentialRequestOptionsDto dto =
        nl.markpost.demo.authentication.dto.PublicKeyCredentialRequestOptionsDto.from(
            assertionRequest.getPublicKeyCredentialRequestOptions()
        );
    //TODO: mapstruct mapper PublicKeyCredentialRequestOptions to PublicKeyCredentialRequestOptionsDto
    //TODO: remove nl.markpost.demo.authentication.dto.PublicKeyCredentialRequestOptionsDto
    return ResponseEntity.ok(dto);
  }

  @Override
  public ResponseEntity<Message> finishPasskeyLogin(
      PasskeyLoginFinishRequest request) {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    HttpSession session = attrs.getRequest().getSession(true);
    AssertionRequest assertionRequest = (AssertionRequest) session.getAttribute("webauthn_assertion_request");

    // Convert the credential DTO to WebAuthn library type
    ObjectMapper objectMapper = new ObjectMapper();
    PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential =
        objectMapper.convertValue(request.getCredential(),
            objectMapper.getTypeFactory().constructParametricType(
                PublicKeyCredential.class,
                AuthenticatorAssertionResponse.class,
                ClientAssertionExtensionOutputs.class));

    ResponseEntity<Message> result = passkeyService.finishAuthentication(
        request.getEmail(), credential, assertionRequest);
    session.removeAttribute("webauthn_assertion_request");
    return result;
  }

  @Override
  public ResponseEntity<PublicKeyCredentialRequestOptionsDto> startUsernamelessPasskeyLogin() {
    AssertionRequest assertionRequest = passkeyService.startUsernamelessAuthentication();
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    HttpSession session = attrs.getRequest().getSession(true);
    session.setAttribute("webauthn_usernameless_assertion_request", assertionRequest);

    // Return custom DTO that properly excludes null values
    nl.markpost.demo.authentication.dto.PublicKeyCredentialRequestOptionsDto dto =
        nl.markpost.demo.authentication.dto.PublicKeyCredentialRequestOptionsDto.from(
            assertionRequest.getPublicKeyCredentialRequestOptions()
        );
    //TODO: mapstruct mapper PublicKeyCredentialRequestOptions to PublicKeyCredentialRequestOptionsDto
    //TODO: remove nl.markpost.demo.authentication.dto.PublicKeyCredentialRequestOptionsDto
    return ResponseEntity.ok(dto);
  }

  @Override
  public ResponseEntity<Message> finishUsernamelessPasskeyLogin(
      PublicKeyCredentialAssertionDto publicKeyCredentialAssertionDto) {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
    HttpSession session = attrs.getRequest().getSession(true);
    AssertionRequest assertionRequest = (AssertionRequest) session.getAttribute("webauthn_usernameless_assertion_request");

    // Convert the credential DTO to WebAuthn library type
    ObjectMapper objectMapper = new ObjectMapper();
    PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential =
        objectMapper.convertValue(publicKeyCredentialAssertionDto,
            objectMapper.getTypeFactory().constructParametricType(
                PublicKeyCredential.class,
                AuthenticatorAssertionResponse.class,
                ClientAssertionExtensionOutputs.class));

    ResponseEntity<Message> result = passkeyService.finishUsernamelessAuthentication(credential, assertionRequest);
    session.removeAttribute("webauthn_usernameless_assertion_request");
    return result;
  }

  /**
   * Delete a passkey for the authenticated user.
   *
   * @param credentialId The ID of the passkey to delete.
   * @return A ResponseEntity with no content.
   */
  @Override
  public ResponseEntity<Void> deletePasskey(String credentialId) {
    passkeyService.deletePasskey(credentialId);
    return ResponseEntity.noContent().build();
  }
}
