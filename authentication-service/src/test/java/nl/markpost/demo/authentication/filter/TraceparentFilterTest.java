package nl.markpost.demo.authentication.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.MDC;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class TraceparentFilterTest {

  @Mock
  HttpServletRequest request;

  @Mock
  ServletResponse response;

  @Mock
  FilterChain chain;

  @InjectMocks
  TraceparentFilter filter;

  @Test
  void testDoFilter_withTraceparentHeader() throws Exception {
    val traceparent = "00-abcdef1234567890abcdef1234567890-abcdef1234567890-01";
    when(request.getHeader("traceparent")).thenReturn(traceparent);

    filter.doFilter(request, response, chain);

    assertEquals(traceparent, MDC.get("traceparent"));
    verify(chain, times(1)).doFilter(request, response);
  }

  @Test
  void testDoFilter_withoutTraceparentHeader() throws Exception {
    when(request.getHeader("traceparent")).thenReturn(null);

    filter.doFilter(request, response, chain);

    String traceparent = MDC.get("traceparent");
    assertNotNull(traceparent);
    assertTrue(traceparent.startsWith("00-"));
    assertTrue(traceparent.endsWith("-01"));
    verify(chain).doFilter(request, response);
    MDC.clear();
  }
}