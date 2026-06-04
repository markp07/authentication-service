package nl.markpost.authentication.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class PreflightRequestFilterTest {

  @Test
  @DisplayName("Should short-circuit CORS preflight requests")
  void preflightReturnsNoContent() throws Exception {
    PreflightRequestFilter filter = new PreflightRequestFilter();
    MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/v1/test");
    request.addHeader("Access-Control-Request-Method", "POST");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    assertEquals(204, response.getStatus());
    verifyNoInteractions(chain);
  }

  @Test
  @DisplayName("Should continue chain for non-preflight OPTIONS requests")
  void optionsWithoutCorsRequestMethodContinuesChain() throws Exception {
    PreflightRequestFilter filter = new PreflightRequestFilter();
    MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/v1/test");
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain = mock(FilterChain.class);

    filter.doFilter(request, response, chain);

    verify(chain, times(1)).doFilter(request, response);
  }
}

