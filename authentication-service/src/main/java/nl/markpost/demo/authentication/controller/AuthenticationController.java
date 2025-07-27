package nl.markpost.demo.authentication.controller;

import static nl.markpost.demo.authentication.util.MessageResponseUtil.createMessageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.authentication.api.v1.controller.LoginApi;
import nl.markpost.demo.authentication.api.v1.model.LoginRequest;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.api.v1.model.RegisterRequest;
import nl.markpost.demo.authentication.constant.Messages;
import nl.markpost.demo.authentication.service.LoginService;
import nl.markpost.demo.authentication.util.MessageResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling authentication-related requests such as login, logout, refresh, and registration.
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController implements LoginApi {

  private final LoginService loginService;

  /**
   * Handles user login requests.
   *
   * @param loginRequest the login request containing email and password
   * @return a ResponseEntity containing a message indicating success or failure
   */
  @Override
  public ResponseEntity<Message> login(LoginRequest loginRequest) {
    log.info("Login attempt for user: {}", loginRequest.getEmail());
    return loginService.login(loginRequest);
  }

  /**
   * Handles user logout requests.
   *
   * @return a ResponseEntity containing a message indicating successful logout
   */
  @Override
  public ResponseEntity<Message> logout() {
    log.info("Logout request received");
    loginService.logout();
    return ResponseEntity.status(HttpStatus.OK).body(createMessageResponse(Messages.LOGOUT_SUCCESS));
  }

  /**
   * Handles token refresh requests.
   *
   * @return a ResponseEntity containing a message indicating successful token refresh
   */
  @Override
  public ResponseEntity<Message> refresh() {
    log.info("Refresh token request received");
    loginService.refresh();
    return ResponseEntity.status(HttpStatus.OK).body(createMessageResponse(Messages.REFRESH_SUCCESS));
  }

  /**
   * Handles user registration requests.
   *
   * @param registerRequest the registration request containing user details
   * @return a ResponseEntity containing a message indicating successful registration
   */
  @Override
  public ResponseEntity<Message> register(RegisterRequest registerRequest) {
    log.info("Registration attempt for user: {}", registerRequest.getEmail());
    loginService.register(registerRequest);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(MessageResponseUtil.createMessageResponse(Messages.REGISTRATION_SUCCESS));
  }

}
