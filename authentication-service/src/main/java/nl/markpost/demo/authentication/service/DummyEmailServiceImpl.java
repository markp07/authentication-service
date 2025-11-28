package nl.markpost.demo.authentication.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "email.service.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class DummyEmailServiceImpl implements EmailService {

  @Value("${email.from}")
  private String from;

  @Value("${email.subject.reset-password}")
  private String resetPasswordSubject;

  @Value("${email.body.reset-password}")
  private String resetPasswordBody;

  @Value("${email.subject.email-verification:Verify Your Email Address}")
  private String emailVerificationSubject;

  @Value("${email.body.email-verification:Please verify your email}")
  private String emailVerificationBody;

  @Value("${email.base-url:https://demo.markpost.dev}")
  private String baseUrl;

  @Override
  public void sendResetPasswordEmail(String to, String resetToken, String userName) {
    String body = resetPasswordBody.replace("{resetToken}", resetToken)
        .replace("{userName}", userName);
    log.info("[DUMMY EMAIL] To: {}\nSubject: {}\nBody: {}", to, resetPasswordSubject, body);
  }

  @Override
  public void sendEmailVerificationEmail(String to, String verificationToken, String userName) {
    String verificationLink = baseUrl + "/api/auth/v1/email/verify?token=" + verificationToken;
    String body = emailVerificationBody.replace("{verificationLink}", verificationLink)
        .replace("{userName}", userName);
    log.info("[DUMMY EMAIL] To: {}\nSubject: {}\nBody: {}", to, emailVerificationSubject, body);
  }
}
