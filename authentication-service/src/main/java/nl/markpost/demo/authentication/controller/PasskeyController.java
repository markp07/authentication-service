package nl.markpost.demo.authentication.controller;

import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import nl.markpost.demo.authentication.service.PasskeyService;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<List<PasskeyCredential>> listPasskeys(Principal principal) {
    return ResponseEntity.ok(passkeyService.listPasskeys(principal.getName()));
  }

  @DeleteMapping("/{credentialId}")
  public ResponseEntity<Void> deletePasskey(@PathVariable String credentialId,
      Principal principal) {
    passkeyService.deletePasskey(principal.getName(), credentialId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/register/start")
  public ResponseEntity<?> startRegistration() {
    return ResponseEntity.ok(passkeyService.startRegistration());
  }

  @PostMapping("/register/finish")
  public ResponseEntity<Void> finishRegistration(@RequestParam String name,
      @RequestBody PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential) {
    passkeyService.finishRegistration(credential, name);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/login/start")
  public ResponseEntity<?> startAuthentication(@RequestParam String email) {
    return ResponseEntity.ok(passkeyService.startAuthentication(email));
  }

  @PostMapping("/login/finish")
  public ResponseEntity<Message> finishAuthentication(@RequestParam String email,
      @RequestBody PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential) {
    return passkeyService.finishAuthentication(email, credential);
  }
}
