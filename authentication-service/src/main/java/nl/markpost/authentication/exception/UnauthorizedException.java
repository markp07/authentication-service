package nl.markpost.authentication.exception;

import nl.markpost.authentication.constant.GenericErrorCodes;

public class UnauthorizedException extends GenericException {

  public UnauthorizedException() {
    super(GenericErrorCodes.UNAUTHORIZED);
  }

  public UnauthorizedException(String message) {
    super(message, GenericErrorCodes.UNAUTHORIZED);
  }

  public UnauthorizedException(String message, Exception exception) {
    super(message, GenericErrorCodes.UNAUTHORIZED, exception);
  }
}
