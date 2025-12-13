package nl.markpost.authentication.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nl.markpost.authentication.api.v1.controller.PasskeyApi;
import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.api.v1.model.PasskeyInfoDto;
import nl.markpost.authentication.api.v1.model.PasskeyLoginFinishRequest;
import nl.markpost.authentication.api.v1.model.PasskeyLoginStartRequest;
import nl.markpost.authentication.api.v1.model.PublicKeyCredentialAssertionDto;
import nl.markpost.authentication.api.v1.model.PublicKeyCredentialCreationOptionsDto;
import nl.markpost.authentication.api.v1.model.PublicKeyCredentialRequestOptionsDto;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.service.PasskeyService;
import nl.markpost.authentication.util.UserUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for passkey registration and authentication operations.
 */
@RestController
@RequestMapping("/v1")
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
    User user = UserUtil.getUserFromSecurityContext();
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
    passkeyService.finishRegistration(body, name);
    return ResponseEntity.ok().build();
  }


  /**
   * Start the passkey login process.
   *
   * @param request The request containing the user's email.
   * @return A ResponseEntity containing the PublicKeyCredentialRequestOptionsDto.
   */
  @Override
  public ResponseEntity<PublicKeyCredentialRequestOptionsDto> startPasskeyLogin(
      PasskeyLoginStartRequest request) {
    PublicKeyCredentialRequestOptionsDto options = passkeyService.startAuthentication(
        request.getEmail());
    return ResponseEntity.ok(options);
  }

  /**
   * Finish the passkey login process.
   *
   * @param request The request containing the credential and email.
   * @return A ResponseEntity with authentication result message.
   */
  @Override
  public ResponseEntity<Message> finishPasskeyLogin(PasskeyLoginFinishRequest request) {
    return passkeyService.finishAuthentication(request.getEmail(), request.getCredential());
  }

  /**
   * Start the usernameless passkey login process.
   *
   * @return A ResponseEntity containing the PublicKeyCredentialRequestOptionsDto.
   */
  @Override
  public ResponseEntity<PublicKeyCredentialRequestOptionsDto> startUsernamelessPasskeyLogin() {
    PublicKeyCredentialRequestOptionsDto options = passkeyService.startUsernamelessAuthentication();
    return ResponseEntity.ok(options);
  }

  /**
   * Finish the usernameless passkey login process.
   *
   * @param publicKeyCredentialAssertionDto The credential assertion from the client.
   * @return A ResponseEntity with authentication result message.
   */
  @Override
  public ResponseEntity<Message> finishUsernamelessPasskeyLogin(
      PublicKeyCredentialAssertionDto publicKeyCredentialAssertionDto) {
    return passkeyService.finishUsernamelessAuthentication(publicKeyCredentialAssertionDto);
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
