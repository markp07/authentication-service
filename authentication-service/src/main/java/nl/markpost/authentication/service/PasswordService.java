package nl.markpost.authentication.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.api.v1.model.ChangePasswordRequest;
import nl.markpost.authentication.exception.BadRequestException;
import nl.markpost.authentication.exception.NotFoundException;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user passwords, including changing, forgetting, and resetting passwords.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final EmailService emailService;

  /**
   * Changes the password for the given user.
   *
   * @param user                  the user whose password is to be changed
   * @param changePasswordRequest the request containing the old and new passwords
   * @throws BadRequestException if the old password is incorrect, the new password is the same as
   *                             the old one, or the new password does not meet strength
   *                             requirements
   */
  @Transactional
  public void changePassword(User user, ChangePasswordRequest changePasswordRequest) {
    String oldPassword = changePasswordRequest.getOldPassword();
    String newPassword = changePasswordRequest.getNewPassword();

    if (validateOldPassword(user, oldPassword)) {
      //TODO: Use codes for exception
      throw new BadRequestException("Old password is incorrect");
    }

    if (isPasswordStrong(newPassword)) {
      //TODO: Use codes for exception
      throw new BadRequestException("New password does not meet strength requirements");
    }

    if (passwordEncoder.matches(newPassword, user.getPassword())) {
      //TODO: Use codes for exception
      throw new BadRequestException("New password cannot be the same as the old password");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  /**
   * Initiates a password reset process by generating a reset token and sending it to the user's
   * email.
   *
   * @param email the email address of the user requesting a password reset
   *                           TODO: add time in DB for token expiration
   */
  @Transactional
  public void forgotPassword(String email) {
    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null) {
      log.warn("Forgot password request for non-existing user: {}", email);
      return;
    }

    String resetToken = generateSimpleToken();
    user.setResetToken(resetToken);
    user.setResetTokenCreatedAt(LocalDateTime.now());
    userRepository.save(user);
    emailService.sendResetPasswordEmail(user.getEmail(), resetToken, user.getUsername());
  }

  /**
   * Resets the user's password using the provided reset token and new password.
   *
   * @param resetToken  the token sent to the user's email for password reset
   * @param newPassword the new password to set for the user
   *                                                                                                TODO: check time in DB for token expiration
   */
  @Transactional
  public void resetPassword(String resetToken, String newPassword) {
    User user = userRepository.findByResetToken(resetToken);
    if (user == null) {
      throw new NotFoundException("User not found for the provided reset token");
    }
    if (user.getResetTokenCreatedAt() == null
        || Duration.between(user.getResetTokenCreatedAt(), LocalDateTime.now()).toMinutes() > 5) {
      throw new BadRequestException("Reset token expired. Please request a new password reset.");
    }
    if (passwordEncoder.matches(newPassword, user.getPassword())) {
      throw new BadRequestException("New password cannot be the same as the old password");
    }
    user.setPassword(passwordEncoder.encode(newPassword));
    user.setResetToken(null);
    user.setResetTokenCreatedAt(null);
    userRepository.save(user);
  }

  /**
   * Validates the provided old password against the user's current password.
   *
   * @param user        the user whose password is to be validated
   * @param oldPassword the old password to validate
   * @return true if the old password matches the user's current password, false otherwise
   */
  boolean validateOldPassword(User user, String oldPassword) {
    return passwordEncoder.matches(oldPassword, user.getPassword());
  }

  /**
   * Validates the strength of the provided password.
   *
   * @param password the password to validate
   * @return true if the password meets strength requirements, false otherwise
   */
  boolean isPasswordStrong(String password) {
    // Example: at least 8 chars, 1 uppercase, 1 lowercase, 1 digit
    if (password == null) {
      return false;
    }
    return password.length() >= 8 &&
        password.matches(".*[A-Z].*") &&
        password.matches(".*[a-z].*") &&
        password.matches(".*\\d.*");
  }

  /**
   * Generates a simple random alphanumeric token for password reset.
   *
   * @return a random token string
   */
  private String generateSimpleToken() {
    int length = 16;
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder token = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int idx = (int) (ThreadLocalRandom.current().nextDouble() * chars.length());
      token.append(chars.charAt(idx));
    }
    return token.toString();
  }
}
