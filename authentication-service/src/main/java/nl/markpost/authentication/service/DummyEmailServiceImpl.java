package nl.markpost.authentication.service;

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

  @Value("${email.from-name:}")
  private String fromName;

  @Value("${email.subject.reset-password}")
  private String resetPasswordSubject;

  @Value("${email.body.reset-password}")
  private String resetPasswordBody;

  @Value("${email.subject.email-verification:Verify Your Email Address}")
  private String emailVerificationSubject;

  @Value("${email.body.email-verification:Please verify your email}")
  private String emailVerificationBody;

  @Value("${email.base-url:https://auth.markpost.dev}")
  private String baseUrl;

  @Override
  public void sendResetPasswordEmail(String to, String resetToken, String userName) {
    String body = resetPasswordBody.replace("{resetToken}", resetToken)
        .replace("{userName}", userName);
    logEmail(to, resetPasswordSubject, body);
  }

  @Override
  public void sendEmailVerificationEmail(String to, String verificationToken, String userName) {
    String verificationLink = baseUrl + "/verify-email?token=" + verificationToken;
    String manualVerificationLink = baseUrl + "/verify-email";
    String body = emailVerificationBody.replace("{verificationLink}", verificationLink)
        .replace("{manualVerificationLink}", manualVerificationLink)
        .replace("{verificationToken}", verificationToken)
        .replace("{userName}", userName);
    logEmail(to, emailVerificationSubject, body);
  }

  private void logEmail(String to, String subject, String body) {
    String fromDisplay =
        fromName != null && !fromName.isEmpty() ? fromName + " <" + from + ">" : from;
    log.info("[DUMMY EMAIL] From: {}\nTo: {}\nSubject: {}\nBody: {}", fromDisplay, to, subject,
        body);
  }
}
