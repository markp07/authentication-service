package nl.markpost.authentication.exception;

import nl.markpost.authentication.constant.GenericErrorCodes;

public class ForbiddenException extends GenericException {

  public ForbiddenException() {
    super(GenericErrorCodes.FORBIDDEN);
  }

  public ForbiddenException(String message) {
    super(message, GenericErrorCodes.FORBIDDEN);
  }

  public ForbiddenException(String message, Exception exception) {
    super(message, GenericErrorCodes.FORBIDDEN, exception);
  }

}
