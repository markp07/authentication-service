package nl.markpost.demo.weather.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.common.exception.UnauthorizedException;
import nl.markpost.demo.common.model.Error;
import nl.markpost.demo.weather.exception.CustomExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter for authenticating requests using JWT access tokens.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final CustomExceptionHandler customExceptionHandler;

  private final ObjectMapper objectMapper;

  private static final String AUTH_SERVICE_PUBLIC_KEY_URL = "http://localhost:12002/v1/public-key";
  private static final AtomicReference<PublicKey> cachedPublicKey = new AtomicReference<>();

  @Value("${security.excluded-paths}")
  private String[] excludedPaths;

  //TODO: cleanup and refactor -- Can we move to common package?
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) {
    try {
      handleAuthentication(request, response, filterChain);
    } catch (UnauthorizedException e) {
      log.info("Unauthorized access attempt: {}", e.getMessage());
      ResponseEntity<Error> errorResponse = customExceptionHandler.handleGenericExceptionException(e);
      response.setContentType("application/json");
      response.setStatus(e.getHttpStatus().value());
      addCorsHeaders(request, response);
      try {
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      } catch (IOException ioException) {
        log.error("Error writing response: {}", ioException.getMessage(), ioException);
      }
    } catch (Exception e) {
      log.error("Error during JWT authentication: {}", e.getMessage(), e);
      ResponseEntity<Error> errorResponse = customExceptionHandler.handleException(e);
      response.setContentType("application/json");
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
      addCorsHeaders(request, response);

      try {
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      } catch (IOException ioException) {
        log.error("Error writing response: {}", ioException.getMessage(), ioException);
      }
    }

  }

  private void handleAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws Exception {
    String path = request.getRequestURI();
    if (excludedPaths != null && List.of(excludedPaths).contains(path) || isPreflightRequest(
        request)) {
      filterChain.doFilter(request, response);
      return;
    }

    String accessToken = extractAccessToken(request);
    if (accessToken == null) {
      log.info("No access token found in request");
      response.setStatus(401);
      throw new UnauthorizedException();
    }
    try {
      PublicKey publicKey = getOrFetchPublicKey();
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(publicKey)
          .build()
          .parseClaimsJws(accessToken)
          .getBody();
      request.setAttribute("jwtClaims", claims);
      String email = claims.getSubject();

      if (email == null) {
        log.info("JWT claims do not contain email subject");
        throw new UnauthorizedException();
      }

      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        setAuthentication(email, request);
      }

      log.info("Authorized - Validated JWT for user: {}", email);
      filterChain.doFilter(request, response);
    } catch (Exception e) {
      log.info("JWT validation failed: {}", e.getMessage());
      throw new UnauthorizedException();
    }
  }

  /**
   * Checks if the request is a CORS preflight (OPTIONS) request.
   */
  boolean isPreflightRequest(HttpServletRequest request) {
    return "OPTIONS".equalsIgnoreCase(request.getMethod());
  }

  /**
   * Extracts the access_token from cookies.
   */
  String extractAccessToken(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }
    for (Cookie cookie : request.getCookies()) {
      if ("access_token".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

  /**
   * Gets the cached public key or fetches it from the authentication service if not cached.
   */
  PublicKey getOrFetchPublicKey() throws Exception {
    PublicKey key = cachedPublicKey.get();
    if (key != null) {
      return key;
    }
    PublicKey fetchedKey = fetchPublicKeyFromAuthService();
    cachedPublicKey.set(fetchedKey);
    return fetchedKey;
  }

  /**
   * Fetches the public key from the authentication service.
   */
  PublicKey fetchPublicKeyFromAuthService() throws Exception {
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(AUTH_SERVICE_PUBLIC_KEY_URL))
        .GET()
        .timeout(Duration.ofSeconds(2))
        .build();
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() != 200) {
      throw new IOException("Failed to fetch public key");
    }
    String pem = response.body();
    String publicKeyPEM = pem.replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replaceAll("\\s", "");
    byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
    return KeyFactory.getInstance("RSA").generatePublic(keySpec);
  }

  /**
   * Sets the authentication in the security context based on the email from JWT claims.
   *
   * @param email   the email extracted from JWT claims
   * @param request the HTTP request to set authentication details
   */
  private void setAuthentication(String email, HttpServletRequest request) {
    UserDetails userDetails = new User(email, "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(userDetails, null,
            List.of());
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authToken);
  }

  private void addCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
    String origin = request.getHeader("Origin");
    if (origin != null) {
      response.setHeader("Access-Control-Allow-Origin", origin);
      response.setHeader("Access-Control-Allow-Credentials", "true");
    }
  }
}
