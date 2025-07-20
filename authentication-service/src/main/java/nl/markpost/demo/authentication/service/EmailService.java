package nl.markpost.demo.authentication.service;

public interface EmailService {

  void sendResetPasswordEmail(String to, String resetToken, String userName);

}
