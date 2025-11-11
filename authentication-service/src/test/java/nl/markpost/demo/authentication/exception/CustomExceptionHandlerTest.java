package nl.markpost.demo.authentication.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.yubico.webauthn.exception.AssertionFailedException;
import nl.markpost.demo.common.constant.GenericErrorCodes;
import nl.markpost.demo.common.model.Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class CustomExceptionHandlerTest {

  private CustomExceptionHandler customExceptionHandler;

  @BeforeEach
  void setUp() {
    customExceptionHandler = new CustomExceptionHandler();
  }

  @Test
  @DisplayName("Should handle AssertionFailedException")
  void handleMethodArgumentNotValidException_assertionFailed() {
    AssertionFailedException exception = new AssertionFailedException("Assertion failed");

    ResponseEntity<Error> response =
        customExceptionHandler.handleMethodArgumentNotValidException(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(GenericErrorCodes.UNAUTHORIZED.getCode(), response.getBody().getCode());
    assertEquals(GenericErrorCodes.UNAUTHORIZED.getMessage(), response.getBody().getMessage());
  }
}
