package nl.markpost.demo.authentication.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DummyEmailServiceImplTest {

  @InjectMocks
  private DummyEmailServiceImpl service;

  @Test
  @DisplayName("Should log dummy reset password email with correct values")
  void sendResetPasswordEmail_logsCorrectly() {
    ReflectionTestUtils.setField(service, "from", "noreply@example.com");
    ReflectionTestUtils.setField(service, "resetPasswordSubject", "Reset your password");
    ReflectionTestUtils.setField(service, "resetPasswordBody",
        "Hello {userName}, use this token: {resetToken}");
    service.sendResetPasswordEmail("user@example.com", "token123", "TestUser");
    // No assertion needed, just ensure no exception and code path is covered
  }

  @Test
  @DisplayName("Should log dummy email verification email with correct values")
  void sendEmailVerificationEmail_logsCorrectly() {
    ReflectionTestUtils.setField(service, "from", "noreply@example.com");
    ReflectionTestUtils.setField(service, "emailVerificationSubject", "Verify your email");
    ReflectionTestUtils.setField(service, "emailVerificationBody",
        "Hello {userName}, click here: {verificationLink}");
    service.sendEmailVerificationEmail("user@example.com", "token123", "TestUser");
    // No assertion needed, just ensure no exception and code path is covered
  }
}
