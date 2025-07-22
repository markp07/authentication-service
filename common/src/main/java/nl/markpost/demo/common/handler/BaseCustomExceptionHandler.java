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

@Slf4j
public class BaseCustomExceptionHandler {

  @ExceptionHandler(GenericException.class)
  ResponseEntity<Error> handleGenericExceptionException(GenericException exception) {
    log.error("An error occurred", exception);
    return ResponseEntity.status(exception.getHttpStatus())
        .body(createError(exception.getErrorCode(), exception.getHttpStatus()));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<Error> handleException(Exception e) {
    log.error("An error occurred", e);
    return ResponseEntity.internalServerError()
        .body(createError(GenericErrorCodes.INTERNAL_SERVER_ERROR));
  }

  private Error createError(GenericErrorCodes errorCode) {
    return createError(errorCode, errorCode.getHttpStatus());
  }

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
