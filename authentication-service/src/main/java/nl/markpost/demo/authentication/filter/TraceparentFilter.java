package nl.markpost.demo.authentication.filter;

import static nl.markpost.demo.authentication.constant.Constants.TRACE_PARENT;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Filter to extract the traceparent header from the HTTP request and set it in the MDC (Mapped
 * Diagnostic Context).
 */
@WebFilter
@Component
public class TraceparentFilter implements Filter {

  /**
   * Extracts the traceparent header from the HTTP request and sets it in the MDC.
   *
   * @param request  the servlet request
   * @param response the servlet response
   * @param chain    the filter chain
   * @throws IOException      if an I/O error occurs during processing
   * @throws ServletException if a servlet error occurs during processing
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    String traceparent = null;
    if (request instanceof HttpServletRequest httpRequest) {
      traceparent = httpRequest.getHeader(TRACE_PARENT);
    }

    if (traceparent == null) {
      traceparent = generateTraceparent();
    }

    MDC.put(TRACE_PARENT, traceparent);
    chain.doFilter(request, response);
  }

  /**
   * Generates a new traceparent value.
   *
   * @return the generated traceparent value
   */
  private String generateTraceparent() {
    String traceId = UUID.randomUUID().toString().replace("-", "");
    String spanId = UUID.randomUUID().toString().substring(0, 16);
    return "00-" + traceId + "-" + spanId + "-01";
  }
}
