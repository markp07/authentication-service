package nl.markpost.demo.common.exception;

import nl.markpost.demo.common.constant.GenericErrorCodes;

public class InternalServerErrorException extends GenericException {

  public InternalServerErrorException() {
    super(GenericErrorCodes.INTERNAL_SERVER_ERROR);
  }

  public InternalServerErrorException(String message) {
    super(message, GenericErrorCodes.INTERNAL_SERVER_ERROR);
  }

  public InternalServerErrorException(String message, Exception exception) {
    super(message, GenericErrorCodes.INTERNAL_SERVER_ERROR, exception);
  }

}
