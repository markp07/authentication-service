package nl.markpost.demo.weather.security;

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
import java.util.concurrent.atomic.AtomicReference;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

//TODO: CLeanup, refactor and revisit logic

/**
 * Servlet filter for authenticating requests using JWT access tokens.
 * <p>
 * - Extracts the access_token from cookies.
 * - Validates the JWT using the public key from the authentication service.
 * - Caches the public key for efficiency.
 * - Allows CORS preflight (OPTIONS) requests to pass through.
 * </p>
 */
@Component
@Profile("!ut")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTH_SERVICE_PUBLIC_KEY_URL = "http://localhost:12002/v1/public-key";
  private static final AtomicReference<PublicKey> cachedPublicKey = new AtomicReference<>();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    if (isPreflightRequest(request)) {
      filterChain.doFilter(request, response);
      return;
    }
    String accessToken = extractAccessToken(request);
    if (accessToken == null) {
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
      filterChain.doFilter(request, response);
    } catch (Exception e) {
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
    if (request.getCookies() == null) return null;
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
}
