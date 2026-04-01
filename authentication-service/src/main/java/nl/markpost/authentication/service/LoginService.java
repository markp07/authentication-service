package nl.markpost.authentication.service;

import static nl.markpost.authentication.constant.Constants.ACCESS_TOKEN;
import static nl.markpost.authentication.constant.Constants.DAYS_7;
import static nl.markpost.authentication.constant.Constants.MINUTES_15;
import static nl.markpost.authentication.constant.Constants.MINUTES_5;
import static nl.markpost.authentication.constant.Constants.REFRESH_TOKEN;
import static nl.markpost.authentication.constant.Constants.TEMPORARY_TOKEN;
import static nl.markpost.authentication.util.MessageResponseUtil.createMessageResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.api.v1.model.LoginRequest;
import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.api.v1.model.RegisterRequest;
import nl.markpost.authentication.config.AccountLockoutProperties;
import nl.markpost.authentication.constant.Messages;
import nl.markpost.authentication.exception.BadRequestException;
import nl.markpost.authentication.exception.InternalServerErrorException;
import nl.markpost.authentication.exception.TooManyRequestsException;
import nl.markpost.authentication.exception.UnauthorizedException;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.UserRepository;
import nl.markpost.authentication.util.CookieUtil;
import nl.markpost.authentication.util.RequestUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling user login, registration, and token management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final PasswordService passwordService;
  private final UserService userService;
  private final AccountLockoutProperties lockoutProperties;

  /**
   * Handles user login.
   *
   * @param loginRequest the login request containing email and password
   * @return ResponseEntity with a message indicating success or failure
   */
  @Transactional
  public ResponseEntity<Message> login(LoginRequest loginRequest) {
    User user = userRepository.findByEmail(loginRequest.getEmail())
        .orElseThrow(UnauthorizedException::new);

    if (!user.isAccountNonLocked()) {
      log.warn("Login attempt for locked account: {}", loginRequest.getEmail());
      throw new TooManyRequestsException(Messages.ACCOUNT_LOCKED.getDescription());
    }

    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      handleFailedLoginAttempt(user);
      throw new UnauthorizedException();
    }

    resetFailedLoginAttempts(user);

    HttpServletResponse response = RequestUtil.getCurrentResponse();

    if (user.is2faEnabled()) {
      String temporaryToken = jwtService.generateAccessToken(user);
      response.addCookie(CookieUtil.buildCookie(TEMPORARY_TOKEN, temporaryToken, MINUTES_5));

      return ResponseEntity.status(HttpStatus.ACCEPTED)
          .body(createMessageResponse(Messages.TWO_FA_REQUIRED));
    } else {
      String accessToken = jwtService.generateAccessToken(user);
      response.addCookie(CookieUtil.buildCookie(ACCESS_TOKEN, accessToken, MINUTES_15));

      String refreshToken = jwtService.generateRefreshToken(user);
      response.addCookie(CookieUtil.buildCookie(REFRESH_TOKEN, refreshToken, DAYS_7));

      return ResponseEntity.status(HttpStatus.OK)
          .body(createMessageResponse(Messages.LOGIN_SUCCESS));
    }
  }

  private void handleFailedLoginAttempt(User user) {
    int attempts = user.getFailedLoginAttempts() + 1;
    user.setFailedLoginAttempts(attempts);
    if (attempts >= lockoutProperties.getMaxFailedAttempts()) {
      user.setAccountLockedUntil(
          LocalDateTime.now().plusMinutes(lockoutProperties.getLockoutDurationMinutes()));
      log.warn("Account locked due to too many failed attempts: {}", user.getEmail());
    }
    userRepository.save(user);
  }

  private void resetFailedLoginAttempts(User user) {
    if (user.getFailedLoginAttempts() > 0 || user.getAccountLockedUntil() != null) {
      user.setFailedLoginAttempts(0);
      user.setAccountLockedUntil(null);
      userRepository.save(user);
    }
  }

  /**
   * Logs out the user by clearing the access and refresh tokens.
   */
  public void logout() {
    HttpServletResponse response = RequestUtil.getCurrentResponse();
    if (response == null) {
      throw new InternalServerErrorException();
    }
    response.addCookie(CookieUtil.buildCookie(ACCESS_TOKEN));
    response.addCookie(CookieUtil.buildCookie(REFRESH_TOKEN));
  }

  /**
   * Refreshes the access token using the refresh token from cookies.
   */
  public void refresh() {
    HttpServletRequest request = RequestUtil.getCurrentRequest();
    String refreshToken = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (REFRESH_TOKEN.equals(cookie.getName())) {
          refreshToken = cookie.getValue();
          break;
        }
      }
    }
    if (refreshToken == null) {
      throw new UnauthorizedException();
    }
    String email = jwtService.getEmailFromToken(refreshToken);
    User user = userRepository.findByEmail(email).orElseThrow(UnauthorizedException::new);

    HttpServletResponse response = RequestUtil.getCurrentResponse();

    String accessToken = jwtService.generateAccessToken(user);
    response.addCookie(CookieUtil.buildCookie(ACCESS_TOKEN, accessToken, MINUTES_15));

    refreshToken = jwtService.generateRefreshToken(user);
    response.addCookie(CookieUtil.buildCookie(REFRESH_TOKEN, refreshToken, DAYS_7));
  }

  /**
   * Registers a new user.
   *
   * @param registerRequest the registration request containing user details
   */
  @Transactional
  public void register(RegisterRequest registerRequest) {
    User existing = userRepository.findByEmail(registerRequest.getEmail()).orElse(null);
    if (existing != null) {
      throw new BadRequestException();
    }

    String password = registerRequest.getPassword();
    if (!passwordService.isPasswordStrong(password)) {
      //TODO: Use codes for exception
      throw new BadRequestException("New password does not meet strength requirements");
    }

    String userName = registerRequest.getUserName();
    userService.checkIfUserExists(userName, true);

    String email = registerRequest.getEmail();
    userService.checkIfEmailExists(email);

    User user = User.builder()
        .userName(userName)
        .email(email)
        .password(passwordEncoder.encode(password))
        .roles(Collections.singleton("USER"))
        .build();
    userRepository.save(user);
  }
}
