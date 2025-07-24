package nl.markpost.demo.authentication.service;

import static nl.markpost.demo.authentication.constant.Constants.ACCESS_TOKEN;
import static nl.markpost.demo.authentication.constant.Constants.DAYS_7;
import static nl.markpost.demo.authentication.constant.Constants.MINUTES_15;
import static nl.markpost.demo.authentication.constant.Constants.REFRESH_TOKEN;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.authentication.api.v1.model.LoginRequest;
import nl.markpost.demo.authentication.api.v1.model.RegisterRequest;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.authentication.util.CookieUtil;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  @Transactional
  public ResponseEntity<Void> register(RegisterRequest registerRequest) {
    if (userRepository.findByEmail(registerRequest.getEmail()) != null) {
      return ResponseEntity.badRequest().build();
    }
    //TODO: validate user name and email format
    User user = User.builder()
        .userName(registerRequest.getUserName())
        .email(registerRequest.getEmail())
        .password(passwordEncoder.encode(registerRequest.getPassword()))
        .roles(Collections.singleton("USER"))
        .build();
    userRepository.save(user);
    return ResponseEntity.status(201).build();
  }

  public ResponseEntity<Void> login(LoginRequest loginRequest, HttpServletResponse response) {
    User user = userRepository.findByEmail(loginRequest.getEmail());
    if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      throw new UnauthorizedException();
    }
    String accessToken = jwtService.generateAccessToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    response.addCookie(CookieUtil.buildCookie(ACCESS_TOKEN, accessToken, MINUTES_15));
    response.addCookie(CookieUtil.buildCookie(REFRESH_TOKEN, refreshToken, DAYS_7));
    return ResponseEntity.ok().build();
  }

  public ResponseEntity<Void> logout(HttpServletResponse response) {
    response.addCookie(CookieUtil.buildCookie(ACCESS_TOKEN));
    response.addCookie(CookieUtil.buildCookie(REFRESH_TOKEN));
    return ResponseEntity.ok().build();
  }

  public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("refresh_token".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
          break;
        }
      }
    }
    if (refreshToken == null) {
      throw new UnauthorizedException();
    }
    String emailFromToken = jwtService.getEmailFromToken(refreshToken);
    User user = userRepository.findByEmail(emailFromToken);
    if (user == null) {
      throw new UnauthorizedException();
    }
    String accessToken = jwtService.generateAccessToken(user);
    response.addCookie(CookieUtil.buildCookie(ACCESS_TOKEN, accessToken, DAYS_7));
    return ResponseEntity.ok().build();
  }
}
