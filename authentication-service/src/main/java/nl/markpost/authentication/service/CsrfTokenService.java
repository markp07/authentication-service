package nl.markpost.authentication.service;

import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.api.v1.model.CsrfTokenResponse;
import nl.markpost.authentication.util.RequestUtil;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Service;

/**
 * Service for generating CSRF tokens and setting them as cookies.
 */
@Service
@Slf4j
public class CsrfTokenService {

  /**
   * Generates a secure CSRF token, sets it as a non-HttpOnly cookie (so JavaScript can read it),
   * and returns the token in the response body.
   *
   * @return CsrfTokenResponse containing the generated token
   */
  public CsrfTokenResponse generateCsrfToken() {
    Object tokenAttribute = RequestUtil.getCurrentRequest().getAttribute(CsrfToken.class.getName());
    if (!(tokenAttribute instanceof CsrfToken csrfToken)) {
      throw new IllegalStateException("CSRF token is not available on the current request");
    }
    log.debug("CSRF token generated and set by Spring Security");
    return CsrfTokenResponse.builder().token(csrfToken.getToken()).build();
  }
}
