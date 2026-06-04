package nl.markpost.authentication.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Handles CORS preflight requests early and stops further authentication/business processing.
 */
@Component
public class PreflightRequestFilter extends OncePerRequestFilter {

  /**
   * Returns HTTP 204 for valid preflight requests; otherwise continues the normal filter chain.
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    if (isPreflight(request)) {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      return;
    }
    filterChain.doFilter(request, response);
  }

  private boolean isPreflight(HttpServletRequest request) {
    return "OPTIONS".equalsIgnoreCase(request.getMethod())
        && request.getHeader("Access-Control-Request-Method") != null;
  }
}

