package nl.markpost.demo.authentication.service;

import static nl.markpost.demo.authentication.constant.Constants.ACCESS_TOKEN;
import static nl.markpost.demo.authentication.constant.Constants.DAYS_7;
import static nl.markpost.demo.authentication.constant.Constants.MINUTES_15;
import static nl.markpost.demo.authentication.constant.Constants.MINUTES_5;
import static nl.markpost.demo.authentication.constant.Constants.REFRESH_TOKEN;
import static nl.markpost.demo.authentication.constant.Constants.TEMPORARY_TOKEN;
import static nl.markpost.demo.authentication.constant.Messages.TWO_FA_REQUIRED;
import static nl.markpost.demo.authentication.util.MessageResponseUtil.createMessageResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.authentication.api.v1.model.LoginRequest;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.api.v1.model.RegisterRequest;
import nl.markpost.demo.authentication.constant.Messages;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.authentication.util.CookieUtil;
import nl.markpost.demo.authentication.util.MessageResponseUtil;
import nl.markpost.demo.authentication.util.RequestUtil;
import nl.markpost.demo.common.exception.BadRequestException;
import nl.markpost.demo.common.exception.InternalServerErrorException;
import nl.markpost.demo.common.exception.UnauthorizedException;
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

  /**
   * Handles user login.
   *
   * @param loginRequest the login request containing email and password
   * @return ResponseEntity with a message indicating success or failure
   */
  public ResponseEntity<Message> login(LoginRequest loginRequest) {
    User user = userRepository.findByEmail(loginRequest.getEmail());
    if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      throw new UnauthorizedException();
    }
    HttpServletResponse response = RequestUtil.getCurrentResponse();

    if(user.is2faEnabled()) {
      String temporaryToken = jwtService.generateAccessToken(user);
      response.addCookie(CookieUtil.buildCookie(TEMPORARY_TOKEN, temporaryToken, MINUTES_5));

      return ResponseEntity.status(HttpStatus.ACCEPTED).body(createMessageResponse(Messages.TWO_FA_REQUIRED));
    } else {
      String accessToken = jwtService.generateAccessToken(user);
      response.addCookie(CookieUtil.buildCookie(ACCESS_TOKEN, accessToken, MINUTES_15));

      String refreshToken = jwtService.generateRefreshToken(user);
      response.addCookie(CookieUtil.buildCookie(REFRESH_TOKEN, refreshToken, DAYS_7));

      return ResponseEntity.status(HttpStatus.OK).body(createMessageResponse(Messages.LOGIN_SUCCESS));
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
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new UnauthorizedException();
    }

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
    if (userRepository.findByEmail(registerRequest.getEmail()) != null) {
      throw new BadRequestException(); //TODO: Use a more specific message
    }

    //TODO: validate user name and email format

    User user = User.builder()
        .userName(registerRequest.getUserName())
        .email(registerRequest.getEmail())
        .password(passwordEncoder.encode(registerRequest.getPassword()))
        .roles(Collections.singleton("USER"))
        .build();
    userRepository.save(user);
  }
}
