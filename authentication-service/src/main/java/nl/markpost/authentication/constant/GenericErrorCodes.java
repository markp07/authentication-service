package nl.markpost.authentication.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Generic error codes. */
@Getter
@AllArgsConstructor
public enum GenericErrorCodes {

  BAD_REQUEST("BAD_REQUEST", "Bad request", HttpStatus.BAD_REQUEST),
  NOT_FOUND("NOT_FOUND", "Not found", HttpStatus.NOT_FOUND),
  FORBIDDEN("FORBIDDEN", "Forbidden", HttpStatus.FORBIDDEN),
  UNAUTHORIZED("UNAUTHORIZED", "Unauthorized", HttpStatus.UNAUTHORIZED),
  INTERNAL_SERVER_ERROR(
      "INTERNAL_SERVER_ERROR", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
  NOT_IMPLEMENTED("NOT_IMPLEMENTED", "Not implemented", HttpStatus.NOT_IMPLEMENTED),
  SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "Service unavailable", HttpStatus.SERVICE_UNAVAILABLE),
  TOO_MANY_REQUESTS("TOO_MANY_REQUESTS", "Too many requests", HttpStatus.TOO_MANY_REQUESTS),
  ;

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
