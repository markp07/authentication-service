package nl.markpost.authentication.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorsErrorHeaderFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    filterChain.doFilter(request, response);

    if (response.getStatus() >= 400) {
      response.setHeader("Access-Control-Allow-Origin", null);
      response.setHeader("Access-Control-Allow-Credentials", null);
      response.setHeader("Access-Control-Expose-Headers", null);
    }
  }
}

