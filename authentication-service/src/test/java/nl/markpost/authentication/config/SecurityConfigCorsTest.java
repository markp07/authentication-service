package nl.markpost.authentication.config;

import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import nl.markpost.authentication.filter.CorsErrorHeaderFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SecurityConfigCorsTest {

  private final CorsErrorHeaderFilter filter = new CorsErrorHeaderFilter();

  @Test
  @DisplayName("Should strip CORS headers on error responses")
  void stripsCorsHeadersOnErrorResponses() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod("GET");
    MockHttpServletResponse response = new MockHttpServletResponse();

    FilterChain chain = (req, res) -> {
      MockHttpServletResponse servletResponse = (MockHttpServletResponse) response;
      servletResponse.setStatus(401);
      servletResponse.setHeader("Access-Control-Allow-Origin", "https://evil.example");
      servletResponse.setHeader("Access-Control-Allow-Credentials", "true");
      servletResponse.setHeader("Access-Control-Expose-Headers", "*");
    };

    filter.doFilter(request, response, chain);

    assertNull(response.getHeader("Access-Control-Allow-Origin"));
    assertNull(response.getHeader("Access-Control-Allow-Credentials"));
    assertNull(response.getHeader("Access-Control-Expose-Headers"));
  }

  @Test
  @DisplayName("Should keep CORS headers on successful responses")
  void keepsCorsHeadersOnSuccessResponses() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod("GET");
    MockHttpServletResponse response = new MockHttpServletResponse();

    FilterChain chain = (req, res) -> {
      MockHttpServletResponse servletResponse = (MockHttpServletResponse) response;
      servletResponse.setStatus(200);
      servletResponse.setHeader("Access-Control-Allow-Origin", "https://app.example");
    };

    filter.doFilter(request, response, chain);

    org.junit.jupiter.api.Assertions.assertEquals(
        "https://app.example", response.getHeader("Access-Control-Allow-Origin"));
  }
}

