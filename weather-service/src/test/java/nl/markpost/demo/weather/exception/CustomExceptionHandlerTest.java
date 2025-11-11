package nl.markpost.demo.weather.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import nl.markpost.demo.common.constant.GenericErrorCodes;
import nl.markpost.demo.common.exception.GenericException;
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
  @DisplayName("Should inherit base exception handler behavior")
  void handleGenericException_success() {
    GenericException exception = new GenericException(GenericErrorCodes.INTERNAL_SERVER_ERROR);

    ResponseEntity<Error> response =
        customExceptionHandler.handleGenericExceptionException(exception);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(GenericErrorCodes.INTERNAL_SERVER_ERROR.getCode(), response.getBody().getCode());
    assertEquals(GenericErrorCodes.INTERNAL_SERVER_ERROR.getMessage(),
        response.getBody().getMessage());
  }
}
