package nl.markpost.demo.authentication.exception;

import lombok.Getter;
import nl.markpost.demo.authentication.constant.GenericErrorCodes;

@Getter
public class TooManyRequestsException extends GenericException {

  public TooManyRequestsException() {
    super(GenericErrorCodes.TOO_MANY_REQUESTS);
  }

  public TooManyRequestsException(String message) {
    super(message, GenericErrorCodes.TOO_MANY_REQUESTS);
  }

  public TooManyRequestsException(String message, Exception exception) {
    super(message, GenericErrorCodes.TOO_MANY_REQUESTS, exception);
  }
}
