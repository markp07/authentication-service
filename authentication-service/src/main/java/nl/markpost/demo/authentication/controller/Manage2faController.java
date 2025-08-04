package nl.markpost.demo.authentication.controller;

import static nl.markpost.demo.authentication.util.MessageResponseUtil.createMessageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.authentication.api.v1.controller.Manage2faApi;
import nl.markpost.demo.authentication.api.v1.model.BackupCodeResponse;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.api.v1.model.PasswordRequest;
import nl.markpost.demo.authentication.api.v1.model.TOTPCode;
import nl.markpost.demo.authentication.api.v1.model.TOTPSetupResponse;
import nl.markpost.demo.authentication.api.v1.model.TOTPVerifyRequest;
import nl.markpost.demo.authentication.constant.Messages;
import nl.markpost.demo.authentication.service.LoginService;
import nl.markpost.demo.authentication.service.Manage2faService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing two-factor authentication (2FA) setup and verification.
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class Manage2faController implements Manage2faApi {

  private final Manage2faService manage2faService;

  private final LoginService loginService;

  /**
   * Endpoint to initiate the setup of two-factor authentication (2FA).
   *
   * @return ResponseEntity containing the TOTP setup response
   */
  public ResponseEntity<TOTPSetupResponse> setup2FA() {
    log.info("Setting up 2FA for user");
    return ResponseEntity.ok(manage2faService.setup2fa());
  }

  /**
   * Endpoint to enable two-factor authentication (2FA) using a TOTP code.
   *
   * @param code the TOTP code provided by the user
   * @return ResponseEntity with a message indicating success
   */
  public ResponseEntity<Message> enable2FA(TOTPCode code) {
    log.info("Enabling 2FA for user with code: {}", code.getCode());
    manage2faService.enable2fa(code);
    return ResponseEntity.status(HttpStatus.OK)
        .body(createMessageResponse(Messages.TWO_FA_SETUP_SUCCESS));
  }

  /**
   * Endpoint to disable two-factor authentication (2FA).
   *
   * @param passwordRequest the request containing the user's password for verification
   * @return ResponseEntity with a message indicating success
   */
  public ResponseEntity<Message> disable2FA(PasswordRequest passwordRequest) {
    manage2faService.disable2fa(passwordRequest);
    loginService.logout();
    return ResponseEntity.status(HttpStatus.OK)
        .body(createMessageResponse(Messages.TWO_FA_DISABLED));
  }

  /**
   * Endpoint to verify the two-factor authentication (2FA) code.
   *
   * @param request the request containing the TOTP code to verify
   * @return ResponseEntity with a message indicating success
   */
  public ResponseEntity<Message> verify2FA(TOTPVerifyRequest request) {
    log.info("Verifying 2FA");
    manage2faService.verify2fa(request);
    return ResponseEntity.status(HttpStatus.OK).body(createMessageResponse(Messages.LOGIN_SUCCESS));
  }

  /**
   * Endpoint to generate a new 2FA backup code for the current user.
   *
   * @return ResponseEntity containing the backup code
   */
  public ResponseEntity<BackupCodeResponse> generateBackupCode() {
    log.info("Generating 2FA backup code for user");
    return ResponseEntity.ok(manage2faService.generateBackupCode());
  }

  /**
   * Endpoint to reset (disable) 2FA using the backup code.
   *
   * @param backupCode the backup code provided by the user
   * @return ResponseEntity with a message indicating success or failure
   */
  public ResponseEntity<Message> reset2FA(@RequestBody BackupCodeResponse backupCode) {
    log.info("Resetting 2FA using backup code");
    boolean success = manage2faService.reset2faWithBackupCode(backupCode.getBackupCode());
    if (success) {
      loginService.logout();
      return ResponseEntity.status(HttpStatus.OK)
          .body(createMessageResponse(Messages.TWO_FA_DISABLED));
    } else {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(createMessageResponse(Messages.TWO_FA_BACKUP_CODE_INVALID));
    }
  }
}
