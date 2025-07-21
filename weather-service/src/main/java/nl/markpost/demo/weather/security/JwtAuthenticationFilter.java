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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTH_SERVICE_PUBLIC_KEY_URL = "http://localhost:12002/v1/public-key";
  private static final AtomicReference<PublicKey> cachedPublicKey = new AtomicReference<>();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    // Allow CORS preflight requests to pass through without authentication
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      filterChain.doFilter(request, response);
      return;
    }
    String token = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("access_token".equals(cookie.getName())) {
          token = cookie.getValue();
          break;
        }
      }
    }
    if (token == null) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.getWriter().write("Missing access_token cookie");
      return;
    }
    try {
      PublicKey publicKey = getPublicKey();
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(publicKey)
          .build()
          .parseClaimsJws(token)
          .getBody();
      // Optionally, set claims as request attribute for downstream use
      request.setAttribute("jwtClaims", claims);
      filterChain.doFilter(request, response);
    } catch (Exception e) {
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.getWriter().write("Invalid or expired access_token");
    }
  }

  private PublicKey getPublicKey() throws Exception {
    PublicKey key = cachedPublicKey.get();
    if (key != null) {
      return key;
    }
    // Fetch public key from authentication service
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
    PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);
    cachedPublicKey.set(publicKey);
    return publicKey;
  }
}
