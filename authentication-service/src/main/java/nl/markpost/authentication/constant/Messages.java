package nl.markpost.authentication.constant;

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
  TWO_FA_BACKUP_CODE_INVALID("TWO_FA_BACKUP_CODE_INVALID", "Invalid backup code"),
  EMAIL_VERIFIED("EMAIL_VERIFIED", "Email verified successfully"),
  VERIFICATION_EMAIL_SENT("VERIFICATION_EMAIL_SENT", "Verification email sent successfully"),
  ACCOUNT_LOCKED("ACCOUNT_LOCKED", "Account is temporarily locked due to too many failed login attempts"),
  ACCOUNT_BLOCKED("ACCOUNT_BLOCKED", "Account has been blocked by an administrator"),
  ADMIN_BLOCK_SUCCESS("ADMIN_BLOCK_SUCCESS", "User account blocked successfully"),
  ADMIN_UNBLOCK_SUCCESS("ADMIN_UNBLOCK_SUCCESS", "User account unblocked successfully"),
  ADMIN_RESET_LINK_SENT("ADMIN_RESET_LINK_SENT", "Password reset link sent to user"),
  ADMIN_ROLE_ADDED("ADMIN_ROLE_ADDED", "Role added to user successfully"),
  ADMIN_ROLE_REMOVED("ADMIN_ROLE_REMOVED", "Role removed from user successfully");

  private final String code;
  private final String description;
}
