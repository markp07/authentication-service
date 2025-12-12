package nl.markpost.demo.common.exception;

import nl.markpost.demo.common.constant.GenericErrorCodes;

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
