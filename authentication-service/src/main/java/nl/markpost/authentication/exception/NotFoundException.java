package nl.markpost.authentication.exception;

import nl.markpost.authentication.constant.GenericErrorCodes;

public class NotFoundException extends GenericException {

  public NotFoundException() {
    super(GenericErrorCodes.NOT_FOUND);
  }

  public NotFoundException(String message) {
    super(message, GenericErrorCodes.NOT_FOUND);
  }

}
