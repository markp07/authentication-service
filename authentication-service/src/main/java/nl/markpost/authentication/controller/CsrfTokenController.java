package nl.markpost.authentication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.api.v1.controller.CsrfApi;
import nl.markpost.authentication.api.v1.model.CsrfTokenResponse;
import nl.markpost.authentication.service.CsrfTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for generating CSRF tokens.
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class CsrfTokenController implements CsrfApi {

  private final CsrfTokenService csrfTokenService;

  /**
   * Generates a CSRF token and sets it as a cookie accessible to all configured subdomains.
   *
   * @return ResponseEntity containing the generated CSRF token
   */
  @Override
  public ResponseEntity<CsrfTokenResponse> getCsrfToken() {
    log.info("CSRF token request received");
    return ResponseEntity.ok(csrfTokenService.generateCsrfToken());
  }
}
