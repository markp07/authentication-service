package nl.markpost.authentication.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

/**
 * Error response model for API exception handling.
 * <p>
 * Contains details about the error, such as timestamp, status, code, message, and traceparent for
 * distributed tracing.
 * </p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Error {

  /**
   * The timestamp when the error occurred (UTC).
   */
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private @Nullable OffsetDateTime timestamp;

  /**
   * The HTTP status code of the error.
   */
  private @Nullable Integer status;

  /**
   * The error code representing the type of error.
   */
  private @Nullable String code;

  /**
   * The error message describing the error.
   */
  private @Nullable String message;

  /**
   * The traceparent for distributed tracing (if available).
   */
  private @Nullable String traceparent;

  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

  public Integer getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public String getTraceparent() {
    return traceparent;
  }
}
