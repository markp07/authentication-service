package nl.markpost.demo.weather.exception;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.common.exception.BadRequestException;
import nl.markpost.demo.common.exception.ForbiddenException;
import nl.markpost.demo.common.exception.GenericException;
import nl.markpost.demo.common.exception.InternalServerErrorException;
import nl.markpost.demo.common.exception.NotFoundException;
import nl.markpost.demo.common.exception.ServiceUnavailableException;
import nl.markpost.demo.common.exception.TooManyRequestsException;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResponseHandler implements ErrorDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    log.info("Handling response: {} with methodKey {}", response, methodKey);
    return switch (response.status()) {
      case 400 -> new BadRequestException(response.reason());
      case 401 -> new UnauthorizedException("Unauthorized");
      case 403 -> new ForbiddenException("Forbidden");
      case 404 -> new NotFoundException("Not Found");
      case 429 -> new TooManyRequestsException();
      case 500 -> new InternalServerErrorException("Internal Server Error");
      case 503 -> new ServiceUnavailableException("Service Unavailable");
      default -> new GenericException("Unhandled response code: " + response.status());
    };
  }
}
