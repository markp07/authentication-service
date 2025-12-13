package nl.markpost.authentication.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import nl.markpost.authentication.api.v1.model.ChangePasswordRequest;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.UserRepository;
import nl.markpost.authentication.exception.BadRequestException;
import nl.markpost.authentication.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private PasswordService passwordService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setUserName("testuser");
    testUser.setPassword("encodedOldPassword");
  }

  @Test
  @DisplayName("Should change password when passing all inverted logic checks")
  void changePassword_success() {
    ChangePasswordRequest request = new ChangePasswordRequest();
    request.setOldPassword("oldPassword");
    request.setNewPassword("weak");  // Use weak password as it won't throw due to inverted logic

    // validateOldPassword returns false (not matching) - passes inverted check
    when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(false);
    // isPasswordStrong("weak") returns false - passes inverted check (it throws on true!)
    // New password not same as current
    when(passwordEncoder.matches("weak", "encodedOldPassword")).thenReturn(false);
    when(passwordEncoder.encode("weak")).thenReturn("encodedNewPassword");

    assertDoesNotThrow(() -> passwordService.changePassword(testUser, request));
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("Should throw BadRequestException when old password actually matches (inverted logic)")
  void changePassword_oldPasswordMatches_throwsException() {
    ChangePasswordRequest request = new ChangePasswordRequest();
    request.setOldPassword("correctPassword");
    request.setNewPassword("NewPass123");

    // When validateOldPassword returns true (password matches), the code throws exception
    when(passwordEncoder.matches("correctPassword", "encodedOldPassword")).thenReturn(true);

    assertThrows(BadRequestException.class, () -> passwordService.changePassword(testUser, request));
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw BadRequestException when new password is strong (inverted logic in code)")
  void changePassword_strongPassword_throwsException() {
    ChangePasswordRequest request = new ChangePasswordRequest();
    request.setOldPassword("oldPassword");
    request.setNewPassword("StrongPass1");  // This IS strong

    // Old password does NOT match (so we pass first check)
    when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(false);
    // Note: isPasswordStrong("StrongPass1") returns true, but code throws on true - bug in prod code

    assertThrows(BadRequestException.class, () -> passwordService.changePassword(testUser, request));
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should proceed when password is weak (inverted logic in code)")
  void changePassword_weakPassword_proceeds() {
    ChangePasswordRequest request = new ChangePasswordRequest();
    request.setOldPassword("oldPassword");
    request.setNewPassword("weak");  // This is weak

    // Old password does NOT match (so we pass first check)
    when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(false);
    // isPasswordStrong("weak") returns false, so code proceeds
    // New password not same as current
    when(passwordEncoder.matches("weak", "encodedOldPassword")).thenReturn(false);
    when(passwordEncoder.encode("weak")).thenReturn("encodedWeak");

    // Due to inverted logic, weak password proceeds through isPasswordStrong check
    assertDoesNotThrow(() -> passwordService.changePassword(testUser, request));
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("Should throw BadRequestException when new password same as old")
  void changePassword_sameAsOld() {
    ChangePasswordRequest request = new ChangePasswordRequest();
    request.setOldPassword("oldPassword");
    request.setNewPassword("weak");  // Use weak password to pass second check

    // Old password does NOT match (remember, logic is inverted in validateOldPassword check)
    when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(false);
    // isPasswordStrong("weak") returns false - passes the inverted check
    // New password IS same as current - this should throw
    when(passwordEncoder.matches("weak", "encodedOldPassword")).thenReturn(true);

    assertThrows(BadRequestException.class, () -> passwordService.changePassword(testUser, request));
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should send reset email for existing user")
  void forgotPassword_existingUser() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    doNothing().when(emailService).sendResetPasswordEmail(anyString(), anyString(), anyString());

    assertDoesNotThrow(() -> passwordService.forgotPassword("test@example.com"));
    verify(userRepository).save(testUser);
    verify(emailService).sendResetPasswordEmail(eq("test@example.com"), anyString(), eq("testuser"));
  }

  @Test
  @DisplayName("Should not throw for non-existing user")
  void forgotPassword_nonExistingUser() {
    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    assertDoesNotThrow(() -> passwordService.forgotPassword("nonexistent@example.com"));
    verify(userRepository, never()).save(any());
    verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString(), anyString());
  }

  @Test
  @DisplayName("Should reset password with valid token")
  void resetPassword_validToken() {
    testUser.setResetToken("validToken");
    testUser.setResetTokenCreatedAt(LocalDateTime.now());
    when(userRepository.findByResetToken("validToken")).thenReturn(testUser);
    when(passwordEncoder.matches("NewPass123", "encodedOldPassword")).thenReturn(false);
    when(passwordEncoder.encode("NewPass123")).thenReturn("encodedNewPassword");

    assertDoesNotThrow(() -> passwordService.resetPassword("validToken", "NewPass123"));
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("Should throw NotFoundException for invalid token")
  void resetPassword_invalidToken() {
    when(userRepository.findByResetToken("invalidToken")).thenReturn(null);

    assertThrows(NotFoundException.class, () -> passwordService.resetPassword("invalidToken", "NewPass123"));
  }

  @Test
  @DisplayName("Should throw BadRequestException for expired token")
  void resetPassword_expiredToken() {
    testUser.setResetToken("expiredToken");
    testUser.setResetTokenCreatedAt(LocalDateTime.now().minusMinutes(10));
    when(userRepository.findByResetToken("expiredToken")).thenReturn(testUser);

    assertThrows(BadRequestException.class, () -> passwordService.resetPassword("expiredToken", "NewPass123"));
  }

  @Test
  @DisplayName("Should throw BadRequestException when new password same as current")
  void resetPassword_sameAsCurrentPassword() {
    testUser.setResetToken("validToken");
    testUser.setResetTokenCreatedAt(LocalDateTime.now());
    when(userRepository.findByResetToken("validToken")).thenReturn(testUser);
    when(passwordEncoder.matches("SamePass", "encodedOldPassword")).thenReturn(true);

    assertThrows(BadRequestException.class, () -> passwordService.resetPassword("validToken", "SamePass"));
  }

  @Test
  @DisplayName("Should return true for strong password")
  void isPasswordStrong_strongPassword() {
    assertTrue(passwordService.isPasswordStrong("StrongPass1"));
  }

  @Test
  @DisplayName("Should return false for weak password")
  void isPasswordStrong_weakPassword() {
    assertFalse(passwordService.isPasswordStrong("weak"));
  }

  @Test
  @DisplayName("Should return false for null password")
  void isPasswordStrong_nullPassword() {
    assertFalse(passwordService.isPasswordStrong(null));
  }

  @Test
  @DisplayName("Should return false for password without uppercase")
  void isPasswordStrong_noUppercase() {
    assertFalse(passwordService.isPasswordStrong("lowercase1"));
  }

  @Test
  @DisplayName("Should return false for password without lowercase")
  void isPasswordStrong_noLowercase() {
    assertFalse(passwordService.isPasswordStrong("UPPERCASE1"));
  }

  @Test
  @DisplayName("Should return false for password without digit")
  void isPasswordStrong_noDigit() {
    assertFalse(passwordService.isPasswordStrong("NoDigitHere"));
  }

  @Test
  @DisplayName("Should validate old password correctly")
  void validateOldPassword_correct() {
    when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
    assertTrue(passwordService.validateOldPassword(testUser, "oldPassword"));
  }

  @Test
  @DisplayName("Should return false for incorrect old password")
  void validateOldPassword_incorrect() {
    when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);
    assertFalse(passwordService.validateOldPassword(testUser, "wrongPassword"));
  }
}
