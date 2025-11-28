package nl.markpost.demo.authentication.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.common.exception.BadRequestException;
import nl.markpost.demo.common.exception.TooManyRequestsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private EmailVerificationService emailVerificationService;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail("test@example.com");
    user.setUserName("testuser");
    user.setEmailVerified(false);
  }

  @Test
  @DisplayName("Should send verification email successfully")
  void sendVerificationEmail_success() {
    when(userRepository.save(any(User.class))).thenReturn(user);

    emailVerificationService.sendVerificationEmail(user);

    verify(userRepository).save(user);
    verify(emailService).sendEmailVerificationEmail(any(), any(), any());
  }

  @Test
  @DisplayName("Should throw BadRequestException when email already verified")
  void sendVerificationEmail_emailAlreadyVerified() {
    user.setEmailVerified(true);

    assertThrows(BadRequestException.class,
        () -> emailVerificationService.sendVerificationEmail(user));

    verify(userRepository, never()).save(any());
    verify(emailService, never()).sendEmailVerificationEmail(any(), any(), any());
  }

  @Test
  @DisplayName("Should throw TooManyRequestsException when cooldown not passed")
  void sendVerificationEmail_cooldownNotPassed() {
    user.setLastVerificationEmailSentAt(LocalDateTime.now().minusMinutes(30));

    assertThrows(TooManyRequestsException.class,
        () -> emailVerificationService.sendVerificationEmail(user));

    verify(userRepository, never()).save(any());
    verify(emailService, never()).sendEmailVerificationEmail(any(), any(), any());
  }

  @Test
  @DisplayName("Should send verification email when cooldown passed")
  void sendVerificationEmail_cooldownPassed() {
    user.setLastVerificationEmailSentAt(LocalDateTime.now().minusMinutes(61));
    when(userRepository.save(any(User.class))).thenReturn(user);

    emailVerificationService.sendVerificationEmail(user);

    verify(userRepository).save(user);
    verify(emailService).sendEmailVerificationEmail(any(), any(), any());
  }

  @Test
  @DisplayName("Should verify email successfully")
  void verifyEmail_success() {
    String token = "valid-token";
    user.setEmailVerificationToken(token);
    user.setEmailVerificationTokenCreatedAt(LocalDateTime.now().minusHours(1));
    when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    emailVerificationService.verifyEmail(token);

    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("Should throw BadRequestException for invalid token")
  void verifyEmail_invalidToken() {
    String token = "invalid-token";
    when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.empty());

    assertThrows(BadRequestException.class,
        () -> emailVerificationService.verifyEmail(token));

    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw BadRequestException for expired token")
  void verifyEmail_expiredToken() {
    String token = "expired-token";
    user.setEmailVerificationToken(token);
    user.setEmailVerificationTokenCreatedAt(LocalDateTime.now().minusHours(25));
    when(userRepository.findByEmailVerificationToken(token)).thenReturn(Optional.of(user));

    assertThrows(BadRequestException.class,
        () -> emailVerificationService.verifyEmail(token));

    verify(userRepository, never()).save(any());
  }
}
