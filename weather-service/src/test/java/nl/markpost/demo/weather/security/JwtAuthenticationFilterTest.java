package nl.markpost.demo.weather.security;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.PublicKey;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JwtAuthenticationFilterTest {

  private JwtAuthenticationFilter filter;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    filter = Mockito.spy(new JwtAuthenticationFilter());
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    filterChain = mock(FilterChain.class);
  }

  @Test
  @DisplayName("Should allow preflight OPTIONS requests")
  void doFilterInternal_preflight() throws Exception {
    when(request.getMethod()).thenReturn("OPTIONS");
    filter.doFilterInternal(request, response, filterChain);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("Should throw UnauthorizedException if no access_token cookie")
  void doFilterInternal_noAccessToken() {
    when(request.getMethod()).thenReturn("GET");
    when(request.getCookies()).thenReturn(null);
    assertThrows(UnauthorizedException.class, () ->
        filter.doFilterInternal(request, response, filterChain)
    );
  }

  @Test
  @DisplayName("Should throw UnauthorizedException if JWT validation fails")
  void doFilterInternal_invalidJwt() throws Exception {
    when(request.getMethod()).thenReturn("GET");
    Cookie[] cookies = { new Cookie("access_token", "invalid.jwt.token") };
    when(request.getCookies()).thenReturn(cookies);
    JwtAuthenticationFilter realFilter = new JwtAuthenticationFilter() {
      @Override
      public PublicKey getOrFetchPublicKey() throws Exception {
        throw new RuntimeException("bad key");
      }
    };
    assertThrows(UnauthorizedException.class, () ->
        realFilter.doFilterInternal(request, response, filterChain)
    );
  }

  @Test
  @DisplayName("Should validate JWT and call filterChain if valid")
  void doFilterInternal_validJwt() throws Exception {
    when(request.getMethod()).thenReturn("GET");
    Cookie[] cookies = {new Cookie("access_token", "valid.jwt.token")};
    when(request.getCookies()).thenReturn(cookies);
    JwtAuthenticationFilter realFilter = new JwtAuthenticationFilter() {
      @Override
      PublicKey getOrFetchPublicKey() {
        return mock(PublicKey.class);
      }
    };
    Claims claims = mock(Claims.class);
    var parserBuilder = mock(io.jsonwebtoken.JwtParserBuilder.class);
    var parser = mock(io.jsonwebtoken.JwtParser.class);
    var jws = mock(io.jsonwebtoken.Jws.class);
    when(jws.getBody()).thenReturn(claims);
    when(parser.parseClaimsJws(anyString())).thenReturn(jws);
    when(parserBuilder.setSigningKey(any(PublicKey.class))).thenReturn(parserBuilder);
    when(parserBuilder.build()).thenReturn(parser);
    Mockito.mockStatic(Jwts.class).when(Jwts::parserBuilder).thenReturn(parserBuilder);
    realFilter.doFilterInternal(request, response, filterChain);
    verify(request).setAttribute(eq("jwtClaims"), eq(claims));
    verify(filterChain).doFilter(request, response);
  }
}