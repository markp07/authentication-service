package nl.markpost.demo.authentication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.PublicKey;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.authentication.service.JwtService;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Value;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;

  private final UserDetailsService userDetailsService;

  @Value("${security.excluded-paths}")
  private String[] excludedPaths;

  private final JwtKeyProvider keyProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String path = request.getRequestURI();
    // Ignore authentication for these endpoints
    if (excludedPaths != null && List.of(excludedPaths).contains(path) || isPreflightRequest(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    String accessToken = extractAccessToken(request);
    if (accessToken == null) {
      log.info("No access token found in request");
      throw new UnauthorizedException();
    }

    try {
      PublicKey publicKey = keyProvider.getPublicKey();
      Claims claims = Jwts.parserBuilder()
          .setSigningKey(publicKey)
          .build()
          .parseClaimsJws(accessToken)
          .getBody();
      request.setAttribute("jwtClaims", claims);
      String email = claims.getSubject();

      if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }

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
}
