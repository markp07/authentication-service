package nl.markpost.demo.common.exception;

import nl.markpost.demo.common.constant.GenericErrorCodes;

public class NotFoundException extends GenericException {

  public NotFoundException() {
    super(GenericErrorCodes.NOT_FOUND);
  }

  public NotFoundException(String message) {
    super(message, GenericErrorCodes.NOT_FOUND);
  }

}
