package nl.markpost.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import jakarta.servlet.http.HttpServletRequest;
import nl.markpost.authentication.api.v1.model.CsrfTokenResponse;
import nl.markpost.authentication.util.RequestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

@ExtendWith(MockitoExtension.class)
class CsrfTokenServiceTest {

  @InjectMocks
  private CsrfTokenService csrfTokenService;

  @Test
  @DisplayName("Should return token from Spring Security request attribute")
  void generateCsrfToken_readsRequestAttribute() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    CsrfToken csrfToken = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "token-value");

    try (var mocked = mockStatic(RequestUtil.class)) {
      mocked.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      org.mockito.Mockito.when(mockRequest.getAttribute(CsrfToken.class.getName())).thenReturn(csrfToken);

      CsrfTokenResponse result = csrfTokenService.generateCsrfToken();
      assertEquals("token-value", result.getToken());
    }
  }

  @Test
  @DisplayName("Should fail when CSRF token is not available")
  void generateCsrfToken_throwsWhenTokenMissing() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);

    try (var mocked = mockStatic(RequestUtil.class)) {
      mocked.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      org.mockito.Mockito.when(mockRequest.getAttribute(CsrfToken.class.getName())).thenReturn(null);

      assertThrows(IllegalStateException.class, () -> csrfTokenService.generateCsrfToken());
    }
  }
}
