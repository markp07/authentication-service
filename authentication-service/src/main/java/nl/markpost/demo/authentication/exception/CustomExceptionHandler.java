package nl.markpost.demo.authentication.exception;

import com.yubico.webauthn.exception.AssertionFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.common.constant.GenericErrorCodes;
import nl.markpost.demo.common.handler.BaseCustomExceptionHandler;
import nl.markpost.demo.common.model.Error;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class CustomExceptionHandler extends BaseCustomExceptionHandler {

  @ExceptionHandler(AssertionFailedException.class)
  ResponseEntity<Error> handleMethodArgumentNotValidException(AssertionFailedException e) {
    log.error("Invalid method argument", e);
    return ResponseEntity.badRequest()
        .body(createError(GenericErrorCodes.UNAUTHORIZED));
  }

}
