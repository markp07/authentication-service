package nl.markpost.authentication.constant;

/**
 * Constants used across the application.
 */
public class Constants {

  private Constants() {

  }

  public static final String TRACE_PARENT = "traceparent";
  public final static String ACCESS_TOKEN = "access_token";
  public final static String TEMPORARY_TOKEN = "temporary_token";
  public final static String REFRESH_TOKEN = "refresh_token";
  public final static Integer DAYS_7 = 60 * 60 * 24 * 7; // 7 days in seconds
  public final static Integer MINUTES_15 = 15 * 60; // 15 minutes in seconds
  public final static Integer MINUTES_5 = 5 * 60; // 15 minutes in seconds
  public final static String PASSKEY_REGISTRATION = "webauthn_registration_options";
  public final static String PASSKEY_ASSERTION_REQUEST = "webauthn_assertion_request";
  public final static String PASSKEY_USERNAMELESS_ASSERTION_REQUEST = "webauthn_usernameless_assertion_request";
  public final static String CSRF_TOKEN = "csrf_token";
  public final static Integer HOURS_1 = 60 * 60; // 1 hour in seconds

}
