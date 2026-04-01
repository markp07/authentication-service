package nl.markpost.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import nl.markpost.authentication.api.v1.model.LoginRequest;
import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.api.v1.model.RegisterRequest;
import nl.markpost.authentication.config.AccountLockoutProperties;
import nl.markpost.authentication.constant.Messages;
import nl.markpost.authentication.exception.BadRequestException;
import nl.markpost.authentication.exception.TooManyRequestsException;
import nl.markpost.authentication.exception.UnauthorizedException;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.UserRepository;
import nl.markpost.authentication.util.RequestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
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

  @Mock
  private AccountLockoutProperties lockoutProperties;

  @InjectMocks
  private LoginService loginService;

  @BeforeEach
  void setUp() {
    lenient().when(lockoutProperties.getMaxFailedAttempts()).thenReturn(5);
    lenient().when(lockoutProperties.getLockoutDurationMinutes()).thenReturn(15);
  }

  @Test
  void login_validUser_no2fa_setsTokensAndReturnsSuccess() {
    LoginRequest req = new LoginRequest();
    req.setEmail("test@example.com");
    req.setPassword("password");
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .password("encoded")
        .is2faEnabled(false)
        .build();
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
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .password("encoded")
        .is2faEnabled(true)
        .build();
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
    lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
    assertThrows(BadRequestException.class, () -> loginService.register(req));
  }

  @Test
  void login_wrongPassword_incrementsFailedAttempts() {
    LoginRequest req = new LoginRequest();
    req.setEmail("test@example.com");
    req.setPassword("wrongpassword");
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .password("encoded")
        .is2faEnabled(false)
        .failedLoginAttempts(0)
        .build();
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    assertThrows(UnauthorizedException.class, () -> loginService.login(req));
    verify(userRepository).save(any(User.class));
  }

  @Test
  void login_lockedAccount_throwsTooManyRequests() {
    LoginRequest req = new LoginRequest();
    req.setEmail("test@example.com");
    req.setPassword("password");
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .password("encoded")
        .is2faEnabled(false)
        .failedLoginAttempts(5)
        .accountLockedUntil(LocalDateTime.now().plusMinutes(10))
        .build();
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    assertThrows(TooManyRequestsException.class, () -> loginService.login(req));
  }

  @Test
  void login_expiredLock_allowsLogin() {
    LoginRequest req = new LoginRequest();
    req.setEmail("test@example.com");
    req.setPassword("password");
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .password("encoded")
        .is2faEnabled(false)
        .failedLoginAttempts(5)
        .accountLockedUntil(LocalDateTime.now().minusMinutes(1))
        .build();
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
    when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);
    try (var mocked = mockStatic(RequestUtil.class)) {
      mocked.when(RequestUtil::getCurrentResponse).thenReturn(mockResponse);
      ResponseEntity<Message> response = loginService.login(req);
      assertEquals(HttpStatus.OK, response.getStatusCode());
      verify(userRepository).save(any(User.class));
    }
  }

  @Test
  void login_reachesMaxAttempts_locksAccount() {
    LoginRequest req = new LoginRequest();
    req.setEmail("test@example.com");
    req.setPassword("wrongpassword");
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .password("encoded")
        .is2faEnabled(false)
        .failedLoginAttempts(4)
        .build();
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
    assertThrows(UnauthorizedException.class, () -> loginService.login(req));
    verify(userRepository).save(any(User.class));
    assertEquals(5, user.getFailedLoginAttempts());
    org.junit.jupiter.api.Assertions.assertNotNull(user.getAccountLockedUntil());
  }
}
