package nl.markpost.demo.authentication.controller;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.api.v1.model.PasskeyInfoDto;
import nl.markpost.demo.authentication.api.v1.model.PasskeyLoginFinishRequest;
import nl.markpost.demo.authentication.api.v1.model.PasskeyLoginRequest;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.service.PasskeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/passkey")
@RequiredArgsConstructor
public class PasskeyController {

  private final PasskeyService passkeyService;

  @GetMapping
  public ResponseEntity<List<PasskeyInfoDto>> listPasskeys() {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    return ResponseEntity.ok(passkeyService.listPasskeys(user));
  }

  @DeleteMapping("/{credentialId}")
  public ResponseEntity<Void> deletePasskey(@PathVariable String credentialId) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    passkeyService.deletePasskey(user, credentialId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/register/start")
  public ResponseEntity<PublicKeyCredentialCreationOptions> startRegistration(HttpSession session) {
    PublicKeyCredentialCreationOptions options = passkeyService.startRegistration();
    session.setAttribute("webauthn_registration_options", options);
    return ResponseEntity.ok(options);
  }

  @PostMapping("/register/finish")
  public ResponseEntity<Void> finishRegistration(@RequestParam String name,
      @RequestBody PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential,
      HttpSession session) {
    PublicKeyCredentialCreationOptions options = (PublicKeyCredentialCreationOptions) session.getAttribute(
        "webauthn_registration_options");
    passkeyService.finishRegistration(credential, name, options);
    session.removeAttribute("webauthn_registration_options");
    return ResponseEntity.ok().build();
  }

  @PostMapping("/login/start")
  public ResponseEntity<PublicKeyCredentialRequestOptions> startAuthentication(
      @RequestBody PasskeyLoginRequest request, HttpSession session) {
    // Get the full AssertionRequest and store it in session
    AssertionRequest assertionRequest = passkeyService.startAuthentication(request.getEmail());
    session.setAttribute("webauthn_assertion_request", assertionRequest);

    // Return only the PublicKeyCredentialRequestOptions object
    return ResponseEntity.ok(assertionRequest.getPublicKeyCredentialRequestOptions());
  }

  @PostMapping("/login/finish")
  public ResponseEntity<Message> finishAuthentication(
      @RequestBody PasskeyLoginFinishRequest request, HttpSession session) {
    // Get the stored AssertionRequest from session
    AssertionRequest assertionRequest = (AssertionRequest) session.getAttribute("webauthn_assertion_request");
    ResponseEntity<Message> result = passkeyService.finishAuthentication(
        request.getEmail(), request.getCredential(), assertionRequest);
    session.removeAttribute("webauthn_assertion_request");
    return result;
  }

  @PostMapping("/login/usernameless/start")
  public ResponseEntity<PublicKeyCredentialRequestOptions> startUsernamelessAuthentication(HttpSession session) {
    // Get the full AssertionRequest and store it in session
    AssertionRequest assertionRequest = passkeyService.startUsernamelessAuthentication();
    session.setAttribute("webauthn_usernameless_assertion_request", assertionRequest);

    // Return only the PublicKeyCredentialRequestOptions object
    return ResponseEntity.ok(assertionRequest.getPublicKeyCredentialRequestOptions());
  }

  @PostMapping("/login/usernameless/finish")
  public ResponseEntity<Message> finishUsernamelessAuthentication(
      @RequestBody PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential,
      HttpSession session) {
    // Get the stored AssertionRequest from session
    AssertionRequest assertionRequest = (AssertionRequest) session.getAttribute("webauthn_usernameless_assertion_request");
    ResponseEntity<Message> result = passkeyService.finishUsernamelessAuthentication(credential, assertionRequest);
    session.removeAttribute("webauthn_usernameless_assertion_request");
    return result;
  }
}
