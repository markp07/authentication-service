package nl.markpost.demo.authentication.exception;

import nl.markpost.demo.authentication.constant.GenericErrorCodes;

public class NotImplementedException extends GenericException {

  public NotImplementedException() {
    super(GenericErrorCodes.NOT_IMPLEMENTED);
  }

  public NotImplementedException(String message) {
    super(message, GenericErrorCodes.NOT_IMPLEMENTED);
  }

}
