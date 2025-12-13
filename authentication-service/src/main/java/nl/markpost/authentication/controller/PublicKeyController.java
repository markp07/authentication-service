package nl.markpost.authentication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.api.v1.controller.PublicKeyApi;
import nl.markpost.authentication.security.JwtKeyProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class PublicKeyController implements PublicKeyApi {

  private final JwtKeyProvider keyProvider;

  /**
   * Endpoint to retrieve the public key used for JWT verification. This is useful for clients to
   * verify JWTs issued by this service.
   *
   * @return The public key in PEM format.
   */
  public ResponseEntity<String> getPublicKey() {
    log.info("Public key request received");
    return ResponseEntity.ok(keyProvider.getPublicKeyAsPem());
  }

}
