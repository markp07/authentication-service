package nl.markpost.demo.authentication.service;

import nl.markpost.demo.authentication.api.v1.model.PasswordRequest;
import nl.markpost.demo.authentication.api.v1.model.TOTPCode;
import nl.markpost.demo.authentication.api.v1.model.TOTPSetupResponse;
import nl.markpost.demo.authentication.api.v1.model.TOTPVerifyRequest;
import nl.markpost.demo.authentication.api.v1.model.BackupCodeResponse;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.authentication.util.RequestUtil;
import nl.markpost.demo.common.exception.BadRequestException;
import nl.markpost.demo.common.exception.ForbiddenException;
import nl.markpost.demo.common.exception.NotFoundException;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

class Manage2faServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private Manage2faService manage2faService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void setup2fa_userNotFound_throwsBadRequest() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        try (var mocked = mockStatic(RequestUtil.class)) {
            mocked.when(RequestUtil::getCurrentRequest).thenReturn(mockRequest);
            when(userRepository.findByEmail(anyString())).thenReturn(null);
            assertThrows(BadRequestException.class, () -> manage2faService.setup2fa());
        }
    }

    @Test
    void verify2fa_invalidToken_throwsUnauthorized() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);
        try (var mocked = mockStatic(RequestUtil.class)) {
            mocked.when(RequestUtil::getCurrentRequest).thenReturn(request);
            TOTPVerifyRequest req = new TOTPVerifyRequest();
            assertThrows(UnauthorizedException.class, () -> manage2faService.verify2fa(req));
        }
    }
}
