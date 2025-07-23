package nl.markpost.demo.weather.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import feign.Request;
import feign.Response;
import java.util.stream.Stream;
import nl.markpost.demo.common.exception.BadRequestException;
import nl.markpost.demo.common.exception.ForbiddenException;
import nl.markpost.demo.common.exception.GenericException;
import nl.markpost.demo.common.exception.InternalServerErrorException;
import nl.markpost.demo.common.exception.NotFoundException;
import nl.markpost.demo.common.exception.ServiceUnavailableException;
import nl.markpost.demo.common.exception.TooManyRequestsException;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ResponseHandlerTest {

  private final ResponseHandler handler = new ResponseHandler();

  private Response buildResponse(int status, String reason) {
    return Response.builder()
        .status(status)
        .reason(reason)
        .request(Request.create(Request.HttpMethod.GET, "", java.util.Map.of(), null, null, null))
        .build();
  }

  static Stream<Arguments> exceptionScenarios() {
    return Stream.of(
        Arguments.of(400, "Bad request", BadRequestException.class, "Bad request"),
        Arguments.of(401, "Unauthorized", UnauthorizedException.class, "Unauthorized"),
        Arguments.of(403, "Forbidden", ForbiddenException.class, "Forbidden"),
        Arguments.of(404, "Not Found", NotFoundException.class, "Not found"),
        Arguments.of(429, "Too Many Requests", TooManyRequestsException.class, "Too many requests"),
        Arguments.of(500, "Internal Server Error", InternalServerErrorException.class, "Internal server error"),
        Arguments.of(503, "Service Unavailable", ServiceUnavailableException.class, "Service unavailable"),
        Arguments.of(418, "I'm a teapot", GenericException.class, "Unhandled response code: 418")
    );
  }

  @ParameterizedTest
  @MethodSource("exceptionScenarios")
  @DisplayName("Should map status to correct exception type")
  void decode_statusToException(int status, String reason, Class<? extends Exception> expectedClass, String expectedMessage) {
    Exception ex = handler.decode("method", buildResponse(status, reason));
    assertInstanceOf(expectedClass, ex);
    assertEquals(expectedMessage, ex.getMessage());
  }

  private Class<?> resolveClass(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}