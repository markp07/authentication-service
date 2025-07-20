package nl.markpost.demo.authentication.controller;

import nl.markpost.demo.authentication.api.v1.controller.ManagePasswordApi;
import nl.markpost.demo.authentication.api.v1.model.ChangePasswordRequest;
import nl.markpost.demo.authentication.api.v1.model.ForgotPasswordRequest;
import nl.markpost.demo.authentication.api.v1.model.ResetPasswordRequest;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.service.PasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class PasswordController implements ManagePasswordApi {

  private final PasswordService passwordService;

  public PasswordController(PasswordService passwordService) {
    this.passwordService = passwordService;
  }

  @Override
  public ResponseEntity<Void> changePassword(ChangePasswordRequest changePasswordRequest) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    //TODO: validate old password == current password
    //TODO: validate new password strength
    //TODO: logout user if password change is successful
    boolean success = passwordService.changePassword(user, changePasswordRequest.getNewPassword());
    //TODO: implement exception handling and proper response
    return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
  }

  @Override
  public ResponseEntity<Void> forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
    String email = forgotPasswordRequest.getEmail();
    boolean success = passwordService.forgotPassword(email);
    //TODO: implement exception handling and proper response
    return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
  }

  @Override
  public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
    boolean success = passwordService.resetPassword(request.getResetToken(),
        request.getNewPassword());
    //TODO: implement exception handling and proper response
    return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
  }
}
