package nl.markpost.authentication.service;

import static nl.markpost.authentication.constant.Constants.CSRF_TOKEN;
import static nl.markpost.authentication.constant.Constants.HOURS_1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import nl.markpost.authentication.api.v1.model.CsrfTokenResponse;
import nl.markpost.authentication.util.RequestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CsrfTokenServiceTest {

  @InjectMocks
  private CsrfTokenService csrfTokenService;

  @Test
  @DisplayName("Should generate a CSRF token and set it as a non-HttpOnly cookie")
  void generateCsrfToken_setsNonHttpOnlyCookieAndReturnsToken() {
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    try (var mocked = mockStatic(RequestUtil.class)) {
      mocked.when(RequestUtil::getCurrentResponse).thenReturn(mockResponse);

      CsrfTokenResponse result = csrfTokenService.generateCsrfToken();

      assertNotNull(result);
      assertNotNull(result.getToken());
      assertFalse(result.getToken().isEmpty());

      verify(mockResponse).addCookie(argThat(cookie ->
          CSRF_TOKEN.equals(cookie.getName())
              && result.getToken().equals(cookie.getValue())
              && !cookie.isHttpOnly()
              && cookie.getMaxAge() == HOURS_1
              && "/".equals(cookie.getPath())
      ));
    }
  }

  @Test
  @DisplayName("Should generate unique tokens on successive calls")
  void generateCsrfToken_generatesUniqueTokens() {
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    try (var mocked = mockStatic(RequestUtil.class)) {
      mocked.when(RequestUtil::getCurrentResponse).thenReturn(mockResponse);

      CsrfTokenResponse first = csrfTokenService.generateCsrfToken();
      CsrfTokenResponse second = csrfTokenService.generateCsrfToken();

      assertNotNull(first.getToken());
      assertNotNull(second.getToken());
      assertFalse(first.getToken().equals(second.getToken()),
          "Successive CSRF tokens should be unique");
    }
  }

  @Test
  @DisplayName("Should generate a URL-safe Base64 token without padding")
  void generateCsrfToken_tokenIsUrlSafeBase64() {
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    try (var mocked = mockStatic(RequestUtil.class)) {
      mocked.when(RequestUtil::getCurrentResponse).thenReturn(mockResponse);

      CsrfTokenResponse result = csrfTokenService.generateCsrfToken();

      String token = result.getToken();
      assertNotNull(token);
      // URL-safe Base64 without padding uses only A-Z, a-z, 0-9, - and _
      assertFalse(token.contains("+"), "Token should not contain '+' (URL-safe Base64)");
      assertFalse(token.contains("/"), "Token should not contain '/' (URL-safe Base64)");
      assertFalse(token.contains("="), "Token should not contain '=' padding");
    }
  }
}
