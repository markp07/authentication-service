package nl.markpost.demo.common.handler;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import nl.markpost.demo.common.constant.GenericErrorCodes;
import nl.markpost.demo.common.exception.GenericException;
import nl.markpost.demo.common.model.Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class BaseCustomExceptionHandlerTest {

  private BaseCustomExceptionHandler baseCustomExceptionHandler;

  @BeforeEach
  void setUp() {
    baseCustomExceptionHandler = new BaseCustomExceptionHandler();
  }

  @Test
  void testHandleGenericExceptionException() {
    GenericException exception = mock(GenericException.class);
    when(exception.getErrorCode()).thenReturn(GenericErrorCodes.INTERNAL_SERVER_ERROR);
    when(exception.getHttpStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

    ResponseEntity<Error> response =
        baseCustomExceptionHandler.handleGenericExceptionException(exception);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(GenericErrorCodes.INTERNAL_SERVER_ERROR.getCode(), response.getBody().getCode());
    assertEquals(GenericErrorCodes.INTERNAL_SERVER_ERROR.getMessage(),
        response.getBody().getMessage());
  }

  @Test
  void testHandleException() {
    Exception exception = new Exception("Test exception");

    ResponseEntity<Error> response = baseCustomExceptionHandler.handleException(exception);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(GenericErrorCodes.INTERNAL_SERVER_ERROR.getCode(), response.getBody().getCode());
    assertEquals(GenericErrorCodes.INTERNAL_SERVER_ERROR.getMessage(),
        response.getBody().getMessage());
  }

}

