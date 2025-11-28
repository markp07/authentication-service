package nl.markpost.demo.authentication.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.authentication.api.v1.controller.EmailApi;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.constant.Messages;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.service.EmailVerificationService;
import nl.markpost.demo.authentication.util.MessageResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class EmailController implements EmailApi {

  private final EmailVerificationService emailVerificationService;

  /**
   * Verifies the user's email address using the provided token.
   *
   * @param token the verification token
   * @return ResponseEntity with a message indicating success
   */
  @Override
  public ResponseEntity<Message> verifyEmail(String token) {
    log.info("Email verification request received");
    emailVerificationService.verifyEmail(token);
    return ResponseEntity.ok(MessageResponseUtil.createMessageResponse(Messages.EMAIL_VERIFIED));
  }

  /**
   * Resends the email verification email to the currently authenticated user.
   * Rate limited to max 1 email per hour.
   *
   * @return ResponseEntity with a message indicating success
   */
  @Override
  public ResponseEntity<Message> resendVerificationEmail() {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.info("Resend verification email request for user: {}", user.getEmail());
    emailVerificationService.sendVerificationEmail(user);
    return ResponseEntity.ok(MessageResponseUtil.createMessageResponse(Messages.VERIFICATION_EMAIL_SENT));
  }
}
