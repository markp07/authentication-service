package nl.markpost.demo.authentication.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
@Slf4j
public class DummyEmailServiceImpl implements EmailService {

  @Value("${email.from}")
  private String from;

  @Value("${email.subject.reset-password}")
  private String resetPasswordSubject;

  @Value("${email.body.reset-password}")
  private String resetPasswordBody;

  @Override
  public void sendResetPasswordEmail(String to, String resetToken, String userName) {
    String body = resetPasswordBody.replace("{resetToken}", resetToken)
        .replace("{userName}", userName);
    log.info("[DUMMY EMAIL] To: {}\nSubject: {}\nBody: {}", to, resetPasswordSubject, body);
  }
}
