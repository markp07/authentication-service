package nl.markpost.demo.authentication.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import nl.markpost.demo.authentication.exception.CustomExceptionHandler;
import nl.markpost.demo.authentication.security.JwtKeyProvider;
import nl.markpost.demo.authentication.service.JwtService;
import nl.markpost.demo.common.exception.UnauthorizedException;
import nl.markpost.demo.common.model.Error;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter for authenticating requests using JWT access tokens.
 */
@Component
@Profile("!ut")
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final UserDetailsService userDetailsService;

  private final CustomExceptionHandler customExceptionHandler;

  private final ObjectMapper objectMapper;

  @Value("${security.excluded-paths}")
  private String[] excludedPaths;

  private final JwtKeyProvider keyProvider;

  //TODO: cleanup and refactor -- Can we move to common package?
  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) {
    try {
      handleAuthentication(request, response, filterChain);
    } catch (UnauthorizedException e) {
      log.info("Unauthorized access attempt: {}", e.getMessage());
      ResponseEntity<nl.markpost.demo.common.model.Error> errorResponse = customExceptionHandler.handleGenericExceptionException(e);
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
   * Sets the authentication in the security context based on the email from JWT claims.
   *
   * @param email   the email extracted from JWT claims
   * @param request the HTTP request to set authentication details
   */
  private void setAuthentication(String email, HttpServletRequest request) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities());
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
