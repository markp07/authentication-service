package nl.markpost.authentication.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.Cookie;
import nl.markpost.authentication.filter.CorsErrorHeaderFilter;
import nl.markpost.authentication.filter.JwtAuthenticationFilter;
import nl.markpost.authentication.filter.TraceparentFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;

class SecurityConfigCsrfTest {

  private final SecurityConfig securityConfig = new SecurityConfig(
      Mockito.mock(TraceparentFilter.class),
      Mockito.mock(JwtAuthenticationFilter.class),
      Mockito.mock(CorsErrorHeaderFilter.class));

  @Test
  @DisplayName("Should issue XSRF-TOKEN cookie with expected attributes")
  void shouldIssueCsrfCookieWithExpectedAttributes() {
    CookieCsrfTokenRepository repository = securityConfig.csrfTokenRepository("example.com", true);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    CsrfToken token = repository.generateToken(request);
    repository.saveToken(token, request, response);

    String setCookie = response.getHeader("Set-Cookie");
    assertTrue(setCookie.contains("XSRF-TOKEN="));
    assertTrue(setCookie.contains("Domain=example.com"));
    assertTrue(setCookie.contains("Path=/"));
    assertTrue(setCookie.contains("Secure"));
    assertFalse(setCookie.contains("HttpOnly"));
  }

  @Test
  @DisplayName("Should use double-submit header name for token validation")
  void shouldUseExpectedHeaderName() {
    CookieCsrfTokenRepository repository = securityConfig.csrfTokenRepository("example.com", true);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    CsrfToken token = repository.generateToken(request);
    repository.saveToken(token, request, response);

    request.setCookies(new Cookie("XSRF-TOKEN", token.getToken()));
    request.addHeader("X-XSRF-TOKEN", token.getToken());

    CsrfToken loaded = repository.loadToken(request);
    assertEquals("X-XSRF-TOKEN", loaded.getHeaderName());
    assertEquals(token.getToken(), loaded.getToken());
  }
}

