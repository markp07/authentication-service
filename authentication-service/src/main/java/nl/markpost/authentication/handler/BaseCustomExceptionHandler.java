package nl.markpost.authentication.handler;

import static nl.markpost.authentication.constant.Constants.TRACE_PARENT;

import java.time.Instant;
import java.time.ZoneOffset;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.constant.GenericErrorCodes;
import nl.markpost.authentication.exception.GenericException;
import nl.markpost.authentication.exception.NotFoundException;
import nl.markpost.authentication.model.CustomError;
import nl.markpost.authentication.model.Error;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Base exception handler for custom and generic exceptions.
 * <p>
 * Handles {@link nl.markpost.authentication.exception.GenericException} and generic {@link Exception}.
 * Returns a structured {@link nl.markpost.authentication.model.Error} response with appropriate HTTP
 * status and error details.
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
   * Handles {@link nl.markpost.authentication.exception.GenericException}. Logs the exception and
   * returns a structured error response with the exception's HTTP status and error code.
   *
   * @param exception the thrown GenericException
   * @return ResponseEntity containing the error details
   */
  @ExceptionHandler(GenericException.class)
  public ResponseEntity<Error> handleGenericExceptionException(GenericException exception) {
    log.error("An error occurred", exception);
    if (exception.getCustomError() != null) {
      return ResponseEntity.status(exception.getHttpStatus())
          .body(createError(exception.getCustomError(), exception.getHttpStatus()));
    }

    return ResponseEntity.status(exception.getHttpStatus())
        .body(createError(exception.getErrorCode(), exception.getHttpStatus()));
  }

  /**
   * Handles all other exceptions not specifically handled elsewhere. Logs the exception and returns
   * a generic internal server error response.
   *
   * @param e the thrown Exception
   * @return ResponseEntity containing the error details
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Error> handleException(Exception e) {
    log.error("An error occurred", e);
    return ResponseEntity.internalServerError()
        .body(createError(GenericErrorCodes.INTERNAL_SERVER_ERROR));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  ResponseEntity<Error> handleNoResourceFoundException(Exception e) {
    return handleGenericExceptionException(new NotFoundException(e.getMessage()));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  ResponseEntity<Error> handleMissingServletRequestParameterException(Exception e) {
    log.error("Missing request parameter", e);
    //TODO: handle specific missing parameter case
    return ResponseEntity.badRequest()
        .body(createError(GenericErrorCodes.BAD_REQUEST));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Error> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    log.error("Invalid method argument", e);
    return ResponseEntity.badRequest()
        .body(createError(GenericErrorCodes.BAD_REQUEST));
  }


  /**
   * Creates an error response using the provided error code and its default HTTP status.
   *
   * @param errorCode the error code
   * @return Error object with details
   */
  protected Error createError(GenericErrorCodes errorCode) {
    return createError(errorCode, errorCode.getHttpStatus());
  }

  /**
   * Creates an error response using the provided error code and HTTP status.
   *
   * @param errorCode the error code
   * @param status    the HTTP status
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

  /**
   * Creates an error response using the provided error code and HTTP status.
   *
   * @param errorCode the error code
   * @param status    the HTTP status
   * @return Error object with details
   */
  private Error createError(CustomError errorCode, HttpStatus status) {
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
