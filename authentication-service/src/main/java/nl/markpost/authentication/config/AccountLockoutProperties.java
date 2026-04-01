package nl.markpost.authentication.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for account lockout (brute force protection).
 */
@Configuration
@ConfigurationProperties(prefix = "account.lockout")
@Getter
@Setter
public class AccountLockoutProperties {

  /**
   * Maximum number of consecutive failed login attempts before the account is locked.
   */
  private int maxFailedAttempts = 5;

  /**
   * Duration in minutes for which the account remains locked after exceeding max failed attempts.
   */
  private int lockoutDurationMinutes = 15;
}
