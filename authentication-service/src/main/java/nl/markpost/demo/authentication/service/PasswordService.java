package nl.markpost.demo.authentication.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.api.v1.model.ChangePasswordRequest;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.common.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final EmailService emailService;

  @Transactional
  public void changePassword(User user, ChangePasswordRequest changePasswordRequest) {
    String oldPassword = changePasswordRequest.getOldPassword();
    String newPassword = changePasswordRequest.getNewPassword();

    if (validateOldPassword(user, oldPassword)) {
      //TODO: Use codes for exception
      throw new BadRequestException("Old password is incorrect");
    }

    if (passwordEncoder.matches(newPassword, user.getPassword())) {
      //TODO: Use codes for exception
      throw new BadRequestException("New password cannot be the same as the old password");
    }

    if (isPasswordStrong(newPassword)) {
      //TODO: Use codes for exception
      throw new BadRequestException("New password does not meet strength requirements");
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  @Transactional
  public boolean forgotPassword(String email) {
    Optional<User> userOpt = Optional.ofNullable(userRepository.findByEmail(email));
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      //TODO: use more simple token -- No UUID, to complex for a simple password reset
      String resetToken = UUID.randomUUID().toString();
      user.setResetToken(resetToken);
      userRepository.save(user);
      emailService.sendResetPasswordEmail(user.getEmail(), resetToken, user.getUsername());
      return true;
    }
    return false;
  }

  @Transactional
  public boolean resetPassword(String resetToken, String newPassword) {
    Optional<User> userOpt = Optional.ofNullable(userRepository.findByResetToken(resetToken));
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      if (passwordEncoder.matches(newPassword, user.getPassword())) {
        // New password is the same as the old password
        return false;
      }
      user.setPassword(passwordEncoder.encode(newPassword));
      user.setResetToken(null);
      userRepository.save(user);
      return true;
    }
    return false;
  }

  public boolean validateOldPassword(User user, String oldPassword) {
    return passwordEncoder.matches(oldPassword, user.getPassword());
  }

  public boolean isPasswordStrong(String password) {
    // Example: at least 8 chars, 1 uppercase, 1 lowercase, 1 digit
    if (password == null) return false;
    return password.length() >= 8 &&
      password.matches(".*[A-Z].*") &&
      password.matches(".*[a-z].*") &&
      password.matches(".*\\d.*");
  }
}
