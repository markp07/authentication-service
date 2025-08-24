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

/**
 * Feign error decoder for mapping HTTP response codes to custom exceptions.
 * <p>
 * This handler is used by Feign clients to convert HTTP error responses into meaningful exceptions
 * for the weather service. Each HTTP status code is mapped to a specific exception type, allowing
 * for fine-grained error handling in the service layer.
 * <ul>
 *   <li>400 - {@link BadRequestException}</li>
 *   <li>401 - {@link UnauthorizedException}</li>
 *   <li>403 - {@link ForbiddenException}</li>
 *   <li>404 - {@link NotFoundException}</li>
 *   <li>429 - {@link TooManyRequestsException}</li>
 *   <li>500 - {@link InternalServerErrorException}</li>
 *   <li>503 - {@link ServiceUnavailableException}</li>
 *   <li>Other codes - {@link GenericException}</li>
 * </ul>
 * </p>
 */
@Slf4j
@Component
public class ResponseHandler implements ErrorDecoder {

  /**
   * Decodes the HTTP response and maps it to a custom exception based on the status code.
   *
   * @param methodKey the Feign client method key
   * @param response  the HTTP response from the downstream service
   * @return the mapped exception
   */
  @Override
  public Exception decode(String methodKey, Response response) {
    log.info("Handling response: {} with methodKey {}", response, methodKey);
    return switch (response.status()) {
      case 400 -> new BadRequestException(response.reason());
      case 401 -> new UnauthorizedException();
      case 403 -> new ForbiddenException();
      case 404 -> new NotFoundException();
      case 429 -> new TooManyRequestsException();
      case 500 -> new InternalServerErrorException();
      case 503 -> new ServiceUnavailableException();
      default -> new GenericException("Unhandled response code: " + response.status());
    };
  }
}
