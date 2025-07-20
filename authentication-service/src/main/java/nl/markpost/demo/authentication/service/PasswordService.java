package nl.markpost.demo.authentication.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
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
  public boolean changePassword(User user, String newPassword) {
    if (passwordEncoder.matches(newPassword, user.getPassword())) {
      // New password is the same as the old password
      return false;
    }
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    return true;
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
}
