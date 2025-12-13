package nl.markpost.authentication.exception;

import nl.markpost.authentication.constant.GenericErrorCodes;

public class ServiceUnavailableException extends GenericException {

  public ServiceUnavailableException() {
    super(GenericErrorCodes.SERVICE_UNAVAILABLE);
  }

  public ServiceUnavailableException(String message) {
    super(message, GenericErrorCodes.SERVICE_UNAVAILABLE);
  }

  public ServiceUnavailableException(String message, Exception exception) {
    super(message, GenericErrorCodes.SERVICE_UNAVAILABLE, exception);
  }
}
