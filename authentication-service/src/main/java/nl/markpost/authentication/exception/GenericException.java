package nl.markpost.authentication.exception;

import lombok.Getter;
import nl.markpost.authentication.constant.GenericErrorCodes;
import nl.markpost.authentication.model.CustomError;
import org.springframework.http.HttpStatus;

@Getter
public class GenericException extends RuntimeException {

  private GenericErrorCodes errorCode = GenericErrorCodes.INTERNAL_SERVER_ERROR;
  private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
  private Exception exception;
  private CustomError customError;

  public GenericException(String message) {
    super(message);
  }

  public GenericException(GenericErrorCodes errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
    this.httpStatus = errorCode.getHttpStatus();
  }

  public GenericException(CustomError customError, HttpStatus httpStatus) {
    super(customError.getMessage());
    this.customError = customError;
    this.httpStatus = httpStatus;
  }

  public GenericException(String message, GenericErrorCodes errorCode) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = errorCode.getHttpStatus();
  }

  public GenericException(String message, GenericErrorCodes errorCode, Exception exception) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = errorCode.getHttpStatus();
    this.exception = exception;
  }
}
