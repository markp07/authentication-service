package nl.markpost.demo.authentication.service;

import jakarta.servlet.http.HttpServletRequest;
import nl.markpost.demo.authentication.api.v1.model.LoginRequest;
import nl.markpost.demo.authentication.api.v1.model.RegisterRequest;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.common.exception.BadRequestException;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import nl.markpost.demo.authentication.constant.Messages;
import nl.markpost.demo.authentication.api.v1.model.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import nl.markpost.demo.authentication.util.RequestUtil;
import nl.markpost.demo.authentication.util.CookieUtil;

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
    void login_invalidUser_throwsUnauthorized() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password");
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        assertThrows(UnauthorizedException.class, () -> loginService.login(req));
    }



    @Test
    void login_validUser_no2fa_setsTokensAndReturnsSuccess() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@example.com");
        req.setPassword("password");
        User user = new User();
        user.setPassword("encoded");
        user.set2faEnabled(false);
        when(userRepository.findByEmail(anyString())).thenReturn(user);
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
        when(userRepository.findByEmail(anyString())).thenReturn(user);
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
    void register_existingUserName_throwsBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@example.com");
        req.setPassword("StrongPass1");
        req.setUserName("existinguser");
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(passwordService.isPasswordStrong(anyString())).thenReturn(true);
        doThrow(new BadRequestException()).when(userService).checkIfUserExists(anyString());
        assertThrows(BadRequestException.class, () -> loginService.register(req));
    }

    @Test
    void register_existingEmail_throwsBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(new User());
        assertThrows(BadRequestException.class, () -> loginService.register(req));
    }

    @Test
    void register_success_savesUser() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@example.com");
        req.setPassword("StrongPass1");
        req.setUserName("newuser");
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        when(passwordService.isPasswordStrong(anyString())).thenReturn(true);
        doNothing().when(userService).checkIfUserExists(anyString());
        doNothing().when(userService).checkIfEmailExists(anyString());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        loginService.register(req);
        verify(userRepository).save(any(User.class));
    }
}
