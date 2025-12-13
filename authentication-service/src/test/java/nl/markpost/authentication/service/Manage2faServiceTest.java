package nl.markpost.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import nl.markpost.authentication.api.v1.model.BackupCodeResponse;
import nl.markpost.authentication.api.v1.model.PasswordRequest;
import nl.markpost.authentication.api.v1.model.TOTPCode;
import nl.markpost.authentication.api.v1.model.TOTPSetupResponse;
import nl.markpost.authentication.api.v1.model.TOTPVerifyRequest;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.UserRepository;
import nl.markpost.authentication.util.RequestUtil;
import nl.markpost.authentication.exception.BadRequestException;
import nl.markpost.authentication.exception.ForbiddenException;
import nl.markpost.authentication.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class Manage2faServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private JwtService jwtService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private Manage2faService manage2faService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setEmail("test@example.com");
    testUser.setUserName("testuser");
    testUser.setPassword("encodedPassword");
    testUser.set2faEnabled(false);
  }

  @Test
  @DisplayName("Should setup 2FA successfully")
  void setup2fa_success() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      TOTPSetupResponse response = manage2faService.setup2fa();

      assertNotNull(response);
      assertNotNull(response.getSecret());
      assertNotNull(response.getOtpUri());
      assertNotNull(response.getQrCodeImage());
      assertTrue(response.getQrCodeImage().startsWith("data:image/png;base64,"));
      verify(userRepository).save(testUser);
    }
  }

  @Test
  @DisplayName("Should throw BadRequestException if 2FA already enabled")
  void setup2fa_alreadyEnabled() {
    testUser.set2faEnabled(true);
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      assertThrows(BadRequestException.class, () -> manage2faService.setup2fa());
    }
  }

  @Test
  @DisplayName("Should throw BadRequestException if user not found")
  void setup2fa_userNotFound() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      assertThrows(BadRequestException.class, () -> manage2faService.setup2fa());
    }
  }

  @Test
  @DisplayName("Should throw BadRequestException if TOTP secret not set up")
  void enable2fa_notSetUp() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    TOTPCode code = new TOTPCode();
    code.setCode("123456");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      assertThrows(BadRequestException.class, () -> manage2faService.enable2fa(code));
    }
  }

  @Test
  @DisplayName("Should throw BadRequestException if TOTP setup expired")
  void enable2fa_setupExpired() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    testUser.setTotpSecret("TESTSECRET12345678901234");
    testUser.setTotpSetupCreatedAt(LocalDateTime.now().minusMinutes(10));
    TOTPCode code = new TOTPCode();
    code.setCode("123456");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      assertThrows(BadRequestException.class, () -> manage2faService.enable2fa(code));
    }
  }

  @Test
  @DisplayName("Should throw UnauthorizedException if no temporary token in verify2fa")
  void verify2fa_noTemporaryToken() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    when(mockRequest.getCookies()).thenReturn(null);
    TOTPVerifyRequest request = new TOTPVerifyRequest();
    request.setCode("123456");

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);

      assertThrows(UnauthorizedException.class, () -> manage2faService.verify2fa(request));
    }
  }

  @Test
  @DisplayName("Should throw ForbiddenException if 2FA not enabled during verify")
  void verify2fa_2faNotEnabled() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    Cookie tempCookie = new Cookie("temporary_token", "tempToken123");
    when(mockRequest.getCookies()).thenReturn(new Cookie[]{tempCookie});
    when(jwtService.getEmailFromToken("tempToken123")).thenReturn("test@example.com");
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    TOTPVerifyRequest request = new TOTPVerifyRequest();
    request.setCode("123456");

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);

      assertThrows(ForbiddenException.class, () -> manage2faService.verify2fa(request));
    }
  }

  @Test
  @DisplayName("Should throw BadRequestException when disabling 2FA not enabled")
  void disable2fa_notEnabled() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    PasswordRequest passwordRequest = new PasswordRequest();
    passwordRequest.setPassword("password");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      assertThrows(BadRequestException.class, () -> manage2faService.disable2fa(passwordRequest));
    }
  }

  @Test
  @DisplayName("Should throw UnauthorizedException when password incorrect during disable")
  void disable2fa_incorrectPassword() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    testUser.set2faEnabled(true);
    PasswordRequest passwordRequest = new PasswordRequest();
    passwordRequest.setPassword("wrongPassword");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      assertThrows(UnauthorizedException.class, () -> manage2faService.disable2fa(passwordRequest));
    }
  }

  @Test
  @DisplayName("Should disable 2FA successfully")
  void disable2fa_success() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    testUser.set2faEnabled(true);
    testUser.setTotpSecret("someSecret");
    PasswordRequest passwordRequest = new PasswordRequest();
    passwordRequest.setPassword("correctPassword");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("correctPassword", "encodedPassword")).thenReturn(true);

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      manage2faService.disable2fa(passwordRequest);

      assertFalse(testUser.is2faEnabled());
      verify(userRepository).save(testUser);
    }
  }

  @Test
  @DisplayName("Should throw BadRequestException when generating backup code without 2FA")
  void generateBackupCode_2faNotEnabled() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      assertThrows(BadRequestException.class, () -> manage2faService.generateBackupCode());
    }
  }

  @Test
  @DisplayName("Should generate backup code successfully")
  void generateBackupCode_success() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    testUser.set2faEnabled(true);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode(any())).thenReturn("hashedBackupCode");

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      BackupCodeResponse response = manage2faService.generateBackupCode();

      assertNotNull(response);
      assertNotNull(response.getBackupCode());
      assertEquals(24, response.getBackupCode().length());
      verify(userRepository).save(testUser);
    }
  }

  @Test
  @DisplayName("Should return false if no backup code stored")
  void reset2faWithBackupCode_noBackupCode() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      boolean result = manage2faService.reset2faWithBackupCode("someCode");

      assertFalse(result);
      verify(userRepository, never()).save(any());
    }
  }

  @Test
  @DisplayName("Should return false if backup code doesn't match")
  void reset2faWithBackupCode_wrongCode() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    testUser.setBackupCode("hashedBackupCode");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("wrongCode", "hashedBackupCode")).thenReturn(false);

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      boolean result = manage2faService.reset2faWithBackupCode("wrongCode");

      assertFalse(result);
      verify(userRepository, never()).save(any());
    }
  }

  @Test
  @DisplayName("Should reset 2FA with valid backup code")
  void reset2faWithBackupCode_success() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    testUser.set2faEnabled(true);
    testUser.setTotpSecret("someSecret");
    testUser.setBackupCode("hashedBackupCode");

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches("validCode", "hashedBackupCode")).thenReturn(true);

    try (var mockedStatic = mockStatic(RequestUtil.class)) {
      mockedStatic.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
      mockedStatic.when(() -> RequestUtil.getEmailFromClaims(mockRequest)).thenReturn("test@example.com");

      boolean result = manage2faService.reset2faWithBackupCode("validCode");

      assertTrue(result);
      assertFalse(testUser.is2faEnabled());
      verify(userRepository).save(testUser);
    }
  }

  @Test
  @DisplayName("Should return false for invalid TOTP code format")
  void verifyTotpCode_invalidFormat() {
    boolean result = manage2faService.verifyTotpCode("INVALIDSECRET", "notacode");
    assertFalse(result);
  }
}
