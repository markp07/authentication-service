package nl.markpost.authentication.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "email.service.enabled", havingValue = "true")
public class EmailServiceImpl implements EmailService {

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
  @Value("${email.base-url:https://auth.yourdomain.tld}")
  private String baseUrl;

  private final JavaMailSender mailSender;

  public EmailServiceImpl(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void sendResetPasswordEmail(String to, String resetToken, String userName) {
    String body = resetPasswordBody.replace("{resetToken}", resetToken)
        .replace("{userName}", userName);
    sendEmail(to, resetPasswordSubject, body);
  }

  @Override
  public void sendEmailVerificationEmail(String to, String verificationToken, String userName) {
    String verificationLink = baseUrl + "/verify-email?token=" + verificationToken;
    String manualVerificationLink = baseUrl + "/verify-email";
    String body = emailVerificationBody.replace("{verificationLink}", verificationLink)
        .replace("{manualVerificationLink}", manualVerificationLink)
        .replace("{verificationToken}", verificationToken)
        .replace("{userName}", userName);
    sendEmail(to, emailVerificationSubject, body);
  }

  private void sendEmail(String to, String subject, String body) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      setFromAddress(helper);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(body, false);
      mailSender.send(message);
    } catch (MessagingException | UnsupportedEncodingException e) {
      throw new RuntimeException("Failed to send email", e);
    }
  }

  private void setFromAddress(MimeMessageHelper helper)
      throws MessagingException, UnsupportedEncodingException {
    if (fromName != null && !fromName.isEmpty()) {
      helper.setFrom(new InternetAddress(from, fromName));
    } else {
      helper.setFrom(from);
    }
  }
}
