package nl.markpost.demo.common.exception;

import nl.markpost.demo.common.constant.GenericErrorCodes;

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
