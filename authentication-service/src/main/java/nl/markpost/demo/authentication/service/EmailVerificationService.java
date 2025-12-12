package nl.markpost.demo.authentication.service;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.authentication.exception.BadRequestException;
import nl.markpost.demo.authentication.exception.TooManyRequestsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

  private static final int TOKEN_EXPIRY_HOURS = 24;
  private static final int RESEND_COOLDOWN_MINUTES = 60;

  private final UserRepository userRepository;
  private final EmailService emailService;

  /**
   * Sends an email verification email to the user if eligible (max 1 per hour).
   *
   * @param user the user to send the verification email to
   * @throws TooManyRequestsException if a verification email was sent within the last hour
   */
  @Transactional
  public void sendVerificationEmail(User user) {
    // Check if user's email is already verified
    if (user.isEmailVerified()) {
      throw new BadRequestException("Email is already verified");
    }

    // Check if cooldown period has passed
    LocalDateTime lastSent = user.getLastVerificationEmailSentAt();
    if (lastSent != null && lastSent.plusMinutes(RESEND_COOLDOWN_MINUTES).isAfter(LocalDateTime.now())) {
      throw new TooManyRequestsException("Please wait before requesting another verification email");
    }

    // Generate verification token
    String token = UUID.randomUUID().toString();
    user.setEmailVerificationToken(token);
    user.setEmailVerificationTokenCreatedAt(LocalDateTime.now());
    user.setLastVerificationEmailSentAt(LocalDateTime.now());
    userRepository.save(user);

    // Send email
    emailService.sendEmailVerificationEmail(user.getEmail(), token, user.getUsername());
    log.info("Verification email sent to user: {}", user.getEmail());
  }

  /**
   * Verifies the user's email address using the provided token.
   *
   * @param token the verification token
   * @throws BadRequestException if the token is invalid or expired
   */
  @Transactional
  public void verifyEmail(String token) {
    User user = userRepository.findByEmailVerificationToken(token)
        .orElseThrow(() -> new BadRequestException("Invalid verification token"));

    // Check if token is expired
    LocalDateTime tokenCreatedAt = user.getEmailVerificationTokenCreatedAt();
    if (tokenCreatedAt == null || tokenCreatedAt.plusHours(TOKEN_EXPIRY_HOURS).isBefore(LocalDateTime.now())) {
      throw new BadRequestException("Verification token has expired");
    }

    // Mark email as verified
    user.setEmailVerified(true);
    user.setEmailVerificationToken(null);
    user.setEmailVerificationTokenCreatedAt(null);
    userRepository.save(user);

    log.info("Email verified for user: {}", user.getEmail());
  }
}
