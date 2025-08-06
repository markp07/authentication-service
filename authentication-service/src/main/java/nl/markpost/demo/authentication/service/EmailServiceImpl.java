package nl.markpost.demo.authentication.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Profile("!local")
public class EmailServiceImpl implements EmailService {

  @Value("${email.from}")
  private String from;
  @Value("${email.subject.reset-password}")
  private String resetPasswordSubject;
  @Value("${email.body.reset-password}")
  private String resetPasswordBody;

  private final JavaMailSender mailSender;

  public EmailServiceImpl(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  @Override
  public void sendResetPasswordEmail(String to, String resetToken, String userName) {
    String body = resetPasswordBody.replace("{resetToken}", resetToken)
        .replace("{userName}", userName);
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(from);
      helper.setTo(to);
      helper.setSubject(resetPasswordSubject);
      helper.setText(body, false);
      mailSender.send(message);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send email", e);
    }
  }
}
