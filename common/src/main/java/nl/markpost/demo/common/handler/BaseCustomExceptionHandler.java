package nl.markpost.demo.common.handler;

import static nl.markpost.demo.common.constant.Constants.TRACE_PARENT;

import java.time.Instant;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.common.constant.GenericErrorCodes;
import nl.markpost.demo.common.exception.GenericException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import nl.markpost.demo.common.model.Error;

/**
 * Base exception handler for custom and generic exceptions.
 * <p>
 * Handles {@link nl.markpost.demo.common.exception.GenericException} and generic {@link Exception}.
 * Returns a structured {@link nl.markpost.demo.common.model.Error} response with appropriate HTTP status and error details.
 * </p>
 * <ul>
 *   <li>Logs all exceptions using SLF4J.</li>
 *   <li>Includes traceparent from MDC for distributed tracing.</li>
 *   <li>Populates error response with timestamp, status, code, message, and traceparent.</li>
 * </ul>
 */
@Slf4j
public class BaseCustomExceptionHandler {

  /**
   * Handles {@link nl.markpost.demo.common.exception.GenericException}.
   * Logs the exception and returns a structured error response with the exception's HTTP status and error code.
   *
   * @param exception the thrown GenericException
   * @return ResponseEntity containing the error details
   */
  @ExceptionHandler(GenericException.class)
  ResponseEntity<Error> handleGenericExceptionException(GenericException exception) {
    log.error("An error occurred", exception);
    return ResponseEntity.status(exception.getHttpStatus())
        .body(createError(exception.getErrorCode(), exception.getHttpStatus()));
  }

  /**
   * Handles all other exceptions not specifically handled elsewhere.
   * Logs the exception and returns a generic internal server error response.
   *
   * @param e the thrown Exception
   * @return ResponseEntity containing the error details
   */
  @ExceptionHandler(Exception.class)
  ResponseEntity<Error> handleException(Exception e) {
    log.error("An error occurred", e);
    return ResponseEntity.internalServerError()
        .body(createError(GenericErrorCodes.INTERNAL_SERVER_ERROR));
  }

  /**
   * Creates an error response using the provided error code and its default HTTP status.
   *
   * @param errorCode the error code
   * @return Error object with details
   */
  private Error createError(GenericErrorCodes errorCode) {
    return createError(errorCode, errorCode.getHttpStatus());
  }

  /**
   * Creates an error response using the provided error code and HTTP status.
   *
   * @param errorCode the error code
   * @param status the HTTP status
   * @return Error object with details
   */
  private Error createError(GenericErrorCodes errorCode, HttpStatus status) {
    String traceparent = MDC.get(TRACE_PARENT);
    return Error.builder()
        .timestamp(Instant.now().atOffset(ZoneOffset.UTC))
        .status(status.value())
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .traceparent(traceparent)
        .build();
  }
}
