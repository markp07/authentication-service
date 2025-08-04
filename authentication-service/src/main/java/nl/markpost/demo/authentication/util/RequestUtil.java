package nl.markpost.demo.authentication.util;

import io.jsonwebtoken.Claims;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.common.exception.BadRequestException;
import nl.markpost.demo.common.exception.InternalServerErrorException;
import nl.markpost.demo.common.model.CustomError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Utility class to retrieve the current HTTP request and response objects, and to extract email
 * from JWT claims.
 */
@Slf4j
public class RequestUtil {

  /**
   * Retrieves the current HTTP request from the RequestContextHolder.
   *
   * @return the current HttpServletRequest
   * @throws InternalServerErrorException if no request object is found in the current context
   */
  public static HttpServletRequest getCurrentRequest() {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null || attrs.getResponse() == null) {
      log.error("No request object found in the current request context.");
      throw new InternalServerErrorException();
    }
    return attrs.getRequest();
  }

  /**
   * Retrieves the current HTTP response from the RequestContextHolder.
   *
   * @return the current HttpServletResponse
   * @throws InternalServerErrorException if no response object is found in the current context
   */
  public static HttpServletResponse getCurrentResponse() {
    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attrs == null || attrs.getResponse() == null) {
      log.error("No response object found in the current request context.");
      throw new InternalServerErrorException();
    }
    return attrs.getResponse();
  }

  /**
   * Extracts the email from JWT claims stored in the request attribute.
   *
   * @param request the HttpServletRequest containing JWT claims
   * @return the email extracted from the JWT claims, or null if not found
   */
  public static String getEmailFromClaims(@Nonnull HttpServletRequest request) {
    Claims claims = (Claims) request.getAttribute("jwtClaims");
    if (claims != null) {
      return claims.getSubject();
    }
    log.error("JWT claims not found in the request attributes.");
    CustomError customError = CustomError.builder()
        .code("AUTHENTICATION_ERROR")
        .message("JWT is invalid.")
        .build();
    throw new BadRequestException(customError);
  }

}
