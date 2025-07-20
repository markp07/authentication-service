package nl.markpost.demo.authentication.controller;

import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.api.v1.controller.ManagePasswordApi;
import nl.markpost.demo.authentication.api.v1.model.ChangePasswordRequest;
import nl.markpost.demo.authentication.api.v1.model.ForgotPasswordRequest;
import nl.markpost.demo.authentication.api.v1.model.ResetPasswordRequest;
import nl.markpost.demo.authentication.service.ManagePasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ManagePasswordController implements ManagePasswordApi {

  private final ManagePasswordService managePasswordService;

  @Override
  public ResponseEntity<Void> changePassword(ChangePasswordRequest request) {
    return managePasswordService.changePassword(request);
  }

  @Override
  public ResponseEntity<Void> forgotPassword(ForgotPasswordRequest request) {
    return managePasswordService.forgotPassword(request);
  }

  @Override
  public ResponseEntity<Void> resetPassword(ResetPasswordRequest request) {
    return managePasswordService.resetPassword(request);
  }

}

