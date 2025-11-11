package nl.markpost.demo.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import nl.markpost.demo.authentication.api.v1.model.LoginRequest;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.api.v1.model.RegisterRequest;
import nl.markpost.demo.authentication.constant.Messages;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.authentication.util.RequestUtil;
import nl.markpost.demo.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

class LoginServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private JwtService jwtService;
  @Mock
  private PasswordService passwordService;
  @Mock
  private UserService userService;
  @InjectMocks
  private LoginService loginService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void login_validUser_no2fa_setsTokensAndReturnsSuccess() {
    LoginRequest req = new LoginRequest();
    req.setEmail("test@example.com");
    req.setPassword("password");
    User user = new User();
    user.setPassword("encoded");
    user.set2faEnabled(false);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
    when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    try (var mocked = mockStatic(RequestUtil.class)) {
      mocked.when(RequestUtil::getCurrentResponse).thenReturn(mockResponse);
      ResponseEntity<Message> response = loginService.login(req);
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals(Messages.LOGIN_SUCCESS.getCode(), response.getBody().getCode());
      verify(mockResponse, times(2)).addCookie(any(Cookie.class));
    }
  }

  @Test
  void login_validUser_with2fa_setsTemporaryTokenAndReturns2faRequired() {
    LoginRequest req = new LoginRequest();
    req.setEmail("test@example.com");
    req.setPassword("password");
    User user = new User();
    user.setPassword("encoded");
    user.set2faEnabled(true);
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(jwtService.generateAccessToken(any())).thenReturn("temporaryToken");
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    try (var mocked = mockStatic(RequestUtil.class)) {
      mocked.when(RequestUtil::getCurrentResponse).thenReturn(mockResponse);
      ResponseEntity<Message> response = loginService.login(req);
      assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
      assertEquals(Messages.TWO_FA_REQUIRED.getCode(), response.getBody().getCode());
      verify(mockResponse, times(1)).addCookie(any(Cookie.class));
    }
  }

  @Test
  void logout_clearsTokens() {
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    try (var mocked = mockStatic(RequestUtil.class)) {
      mocked.when(RequestUtil::getCurrentResponse).thenReturn(mockResponse);
      loginService.logout();
      verify(mockResponse, times(2)).addCookie(any(Cookie.class));
    }
  }

  @Test
  void register_existingEmail_throwsBadRequest() {
    RegisterRequest req = new RegisterRequest();
    req.setEmail("test@example.com");
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
    assertThrows(BadRequestException.class, () -> loginService.register(req));
  }
}
