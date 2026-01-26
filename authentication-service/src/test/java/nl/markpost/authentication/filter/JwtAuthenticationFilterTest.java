package nl.markpost.authentication.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import nl.markpost.authentication.exception.CustomExceptionHandler;
import nl.markpost.authentication.security.JwtKeyProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;

class JwtAuthenticationFilterTest {

  private JwtAuthenticationFilter filter;
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    UserDetailsService userDetailsService = mock(UserDetailsService.class);
    CustomExceptionHandler customExceptionHandler = new CustomExceptionHandler();
    ObjectMapper objectMapper = new ObjectMapper();
    JwtKeyProvider keyProvider = mock(JwtKeyProvider.class);

    filter = new JwtAuthenticationFilter(
        userDetailsService,
        customExceptionHandler,
        objectMapper,
        keyProvider
    );
    request = mock(HttpServletRequest.class);
  }

  @Test
  @DisplayName("Should identify preflight OPTIONS requests")
  void isPreflightRequest_optionsMethod() {
    when(request.getMethod()).thenReturn("OPTIONS");
    assertTrue(filter.isPreflightRequest(request));
  }

  @Test
  @DisplayName("Should not identify GET requests as preflight")
  void isPreflightRequest_getMethod() {
    when(request.getMethod()).thenReturn("GET");
    assertFalse(filter.isPreflightRequest(request));
  }

  @Test
  @DisplayName("Should extract access token from cookies")
  void extractAccessToken_withAccessToken() {
    Cookie[] cookies = {
        new Cookie("other_cookie", "other_value"),
        new Cookie("access_token", "test_token_value")
    };
    when(request.getCookies()).thenReturn(cookies);

    String token = filter.extractAccessToken(request);
    assertEquals("test_token_value", token);
  }

  @Test
  @DisplayName("Should return null when no access token cookie")
  void extractAccessToken_noAccessToken() {
    Cookie[] cookies = {
        new Cookie("other_cookie", "other_value")
    };
    when(request.getCookies()).thenReturn(cookies);

    String token = filter.extractAccessToken(request);
    assertNull(token);
  }

  @Test
  @DisplayName("Should return null when cookies array is null")
  void extractAccessToken_noCookies() {
    when(request.getCookies()).thenReturn(null);

    String token = filter.extractAccessToken(request);
    assertNull(token);
  }
}
