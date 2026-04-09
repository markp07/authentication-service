package nl.markpost.authentication.service;

import static nl.markpost.authentication.constant.Constants.CSRF_TOKEN;
import static nl.markpost.authentication.constant.Constants.HOURS_1;

import jakarta.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.api.v1.model.CsrfTokenResponse;
import nl.markpost.authentication.util.CookieUtil;
import nl.markpost.authentication.util.RequestUtil;
import org.springframework.stereotype.Service;

/**
 * Service for generating CSRF tokens and setting them as cookies.
 */
@Service
@Slf4j
public class CsrfTokenService {

  private static final int TOKEN_BYTE_LENGTH = 32;

  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * Generates a secure CSRF token, sets it as a non-HttpOnly cookie (so JavaScript can read it),
   * and returns the token in the response body.
   *
   * @return CsrfTokenResponse containing the generated token
   */
  public CsrfTokenResponse generateCsrfToken() {
    HttpServletResponse response = RequestUtil.getCurrentResponse();

    byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
    secureRandom.nextBytes(tokenBytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

    response.addCookie(CookieUtil.buildNonHttpOnlyCookie(CSRF_TOKEN, token, HOURS_1));
    log.debug("CSRF token generated and set as cookie");

    return CsrfTokenResponse.builder().token(token).build();
  }
}
