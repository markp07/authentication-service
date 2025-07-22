package nl.markpost.demo.common.exception;

import nl.markpost.demo.common.constant.GenericErrorCodes;

public class BadRequestException extends GenericException {

  public BadRequestException() {
    super(GenericErrorCodes.BAD_REQUEST);
  }

  public BadRequestException(String message) {
    super(message, GenericErrorCodes.BAD_REQUEST);
  }

}
