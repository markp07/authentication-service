package nl.markpost.demo.common.exception;

import nl.markpost.demo.common.constant.GenericErrorCodes;
import nl.markpost.demo.common.model.CustomError;
import org.springframework.http.HttpStatus;

public class BadRequestException extends GenericException {

  public BadRequestException() {
    super(GenericErrorCodes.BAD_REQUEST);
  }

  public BadRequestException(CustomError customError) {
    super(customError, HttpStatus.BAD_REQUEST);
  }

  public BadRequestException(String message) {
    super(message, GenericErrorCodes.BAD_REQUEST);
  }

}
