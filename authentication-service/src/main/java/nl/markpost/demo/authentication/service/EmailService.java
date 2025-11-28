package nl.markpost.demo.authentication.service;

public interface EmailService {

  void sendResetPasswordEmail(String to, String resetToken, String userName);

  void sendEmailVerificationEmail(String to, String verificationToken, String userName);

}
