package nl.markpost.demo.authentication.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

  @Mock
  private JavaMailSender mailSender;

  @InjectMocks
  private EmailServiceImpl service;

  @Test
  @DisplayName("Should send reset password email successfully")
  void sendResetPasswordEmail_success() throws MessagingException {
    ReflectionTestUtils.setField(service, "from", "noreply@example.com");
    ReflectionTestUtils.setField(service, "resetPasswordSubject", "Reset your password");
    ReflectionTestUtils.setField(service, "resetPasswordBody",
        "Hello {userName}, use this token: {resetToken}");
    MimeMessage mimeMessage = mock(MimeMessage.class);
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    doNothing().when(mailSender).send(mimeMessage);
    service.sendResetPasswordEmail("user@example.com", "token123", "TestUser");
    verify(mailSender, times(1)).send(mimeMessage);
  }

  @Test
  @DisplayName("Should throw RuntimeException when MessagingException occurs")
  void sendResetPasswordEmail_messagingException() {
    ReflectionTestUtils.setField(service, "from", "noreply@example.com");
    ReflectionTestUtils.setField(service, "resetPasswordSubject", "Reset your password");
    ReflectionTestUtils.setField(service, "resetPasswordBody",
        "Hello {userName}, use this token: {resetToken}");
    MimeMessage mimeMessage = mock(MimeMessage.class);
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    doThrow(new RuntimeException("Simulated failure")).when(mailSender).send(mimeMessage);
    assertThrows(RuntimeException.class,
        () -> service.sendResetPasswordEmail("user@example.com", "token123", "TestUser"));
  }
}
