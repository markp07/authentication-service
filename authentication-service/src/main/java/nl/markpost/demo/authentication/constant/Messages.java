package nl.markpost.demo.authentication.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Messages {

    LOGIN_SUCCESS("LOGIN_SUCCESS", "Login was successful"),
    TWO_FA_REQUIRED("2FA_REQUIRED", "Login was successful, 2fa required"),
    LOGOUT_SUCCESS("LOGOUT_SUCCESS", "Logout was successful"),
    REGISTRATION_SUCCESS("REGISTRATION_SUCCESS", "Registration was successful"),
    REFRESH_SUCCESS("REFRESH_SUCCESS", "Token refreshed successfully"),
    TWO_FA_SETUP_SUCCESS("TWO_FA_SETUP_SUCCESS", "2fa setup was successful"),
    PASSWORD_CHANGE_SUCCESS("PASSWORD_CHANGE_SUCCESS", "Password changed successfully"),
    RESET_SENT_SUCCESS("RESET_SENT_SUCCESS", "Reset token sent successfully"),
    RESET_SUCCESS("RESET_SUCCESS", "Password reset successfully"),
    ACCOUNT_DELETED("ACCOUNT_DELETED", "Account deleted successfully"),
    TWO_FA_DISABLED("TWO_FA_DISABLED", "2fa disabled successfully"),
    ;

    private final String code;
    private final String description;
}
