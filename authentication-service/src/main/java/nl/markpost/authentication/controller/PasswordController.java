package nl.markpost.authentication.controller;

import static nl.markpost.authentication.util.MessageResponseUtil.createMessageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.api.v1.controller.ManagePasswordApi;
import nl.markpost.authentication.api.v1.model.ChangePasswordRequest;
import nl.markpost.authentication.api.v1.model.ForgotPasswordRequest;
import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.api.v1.model.ResetPasswordRequest;
import nl.markpost.authentication.constant.Messages;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.service.PasswordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing user passwords, including changing, forgetting, and resetting passwords.
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class PasswordController implements ManagePasswordApi {

  private final PasswordService passwordService;

  /**
   * Changes the password for the currently authenticated user.
   *
   * @param changePasswordRequest the request containing the new password
   * @return a response entity indicating success or failure
   */
  public ResponseEntity<Message> changePassword(ChangePasswordRequest changePasswordRequest) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    log.info("Change password request for user: {}", user.getEmail());
    passwordService.changePassword(user, changePasswordRequest);
    return ResponseEntity.status(HttpStatus.OK)
        .body(createMessageResponse(Messages.PASSWORD_CHANGE_SUCCESS));
  }

  /**
   * Handles a forgot password request by sending a reset token to the user's email.
   *
   * @param forgotPasswordRequest the request containing the user's email
   * @return a response entity indicating success or failure
   */
  public ResponseEntity<Message> forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
    String email = forgotPasswordRequest.getEmail();
    log.info("Forgot password request for email: {}", email);
    passwordService.forgotPassword(email);
    return ResponseEntity.status(HttpStatus.OK)
        .body(createMessageResponse(Messages.RESET_SENT_SUCCESS));
  }

  /**
   * Resets the password using a reset token and the new password provided by the user.
   *
   * @param request the request containing the reset token and new password
   * @return a response entity indicating success or failure
   */
  public ResponseEntity<Message> resetPassword(@RequestBody ResetPasswordRequest request) {
    log.info("Reset password request");
    passwordService.resetPassword(request.getResetToken(), request.getNewPassword());
    return ResponseEntity.status(HttpStatus.OK)
        .body(createMessageResponse(Messages.RESET_SUCCESS));
  }
}
