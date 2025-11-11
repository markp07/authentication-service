package nl.markpost.demo.authentication.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.markpost.demo.common.exception.BadRequestException;
import nl.markpost.demo.common.exception.InternalServerErrorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class RequestUtilTest {

  @AfterEach
  void tearDown() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  @DisplayName("Should retrieve current request")
  void getCurrentRequest_success() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    ServletRequestAttributes attrs = new ServletRequestAttributes(request, response);
    RequestContextHolder.setRequestAttributes(attrs);

    HttpServletRequest result = RequestUtil.getCurrentRequest();
    assertNotNull(result);
    assertEquals(request, result);
  }

  @Test
  @DisplayName("Should throw exception when no request attributes")
  void getCurrentRequest_noAttributes() {
    RequestContextHolder.resetRequestAttributes();

    assertThrows(InternalServerErrorException.class, RequestUtil::getCurrentRequest);
  }

  @Test
  @DisplayName("Should retrieve current response")
  void getCurrentResponse_success() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    ServletRequestAttributes attrs = new ServletRequestAttributes(request, response);
    RequestContextHolder.setRequestAttributes(attrs);

    HttpServletResponse result = RequestUtil.getCurrentResponse();
    assertNotNull(result);
    assertEquals(response, result);
  }

  @Test
  @DisplayName("Should throw exception when no response attributes")
  void getCurrentResponse_noAttributes() {
    RequestContextHolder.resetRequestAttributes();

    assertThrows(InternalServerErrorException.class, RequestUtil::getCurrentResponse);
  }

  @Test
  @DisplayName("Should extract email from JWT claims")
  void getEmailFromClaims_success() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    Claims claims = mock(Claims.class);
    when(claims.getSubject()).thenReturn("user@example.com");
    when(request.getAttribute("jwtClaims")).thenReturn(claims);

    String email = RequestUtil.getEmailFromClaims(request);
    assertEquals("user@example.com", email);
  }

  @Test
  @DisplayName("Should throw exception when JWT claims not found")
  void getEmailFromClaims_noClaimsAttribute() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getAttribute("jwtClaims")).thenReturn(null);

    assertThrows(BadRequestException.class, () -> RequestUtil.getEmailFromClaims(request));
  }
}
