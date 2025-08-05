package nl.markpost.demo.authentication.service;

import nl.markpost.demo.authentication.api.v1.model.ChangePasswordRequest;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.common.exception.BadRequestException;
import nl.markpost.demo.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void changePassword_invalidOldPassword_throwsBadRequest() {
        User user = new User();
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("wrong");
        req.setNewPassword("NewPassword1");
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        assertThrows(BadRequestException.class, () -> passwordService.changePassword(user, req));
    }

    @Test
    void resetPassword_userNotFound_throwsNotFound() {
        when(userRepository.findByResetToken(anyString())).thenReturn(null);
        assertThrows(NotFoundException.class, () -> passwordService.resetPassword("token", "NewPassword1"));
    }

    @Test
    void forgotPassword_userNotFound_doesNothing() {
        when(userRepository.findByEmail(anyString())).thenReturn(null);
        passwordService.forgotPassword("notfound@example.com");
        verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString(), anyString());
    }

    @Test
    void forgotPassword_success_sendsEmail() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setUserName("user");
        when(userRepository.findByEmail(anyString())).thenReturn(user);
        passwordService.forgotPassword("user@example.com");
        verify(emailService).sendResetPasswordEmail(eq("user@example.com"), anyString(), eq("user"));
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_expiredToken_throwsBadRequest() {
        User user = new User();
        user.setResetTokenCreatedAt(java.time.LocalDateTime.now().minusMinutes(10));
        when(userRepository.findByResetToken(anyString())).thenReturn(user);
        assertThrows(BadRequestException.class, () -> passwordService.resetPassword("token", "NewPassword1"));
    }

    @Test
    void resetPassword_newPasswordSameAsOld_throwsBadRequest() {
        User user = new User();
        user.setPassword("encoded");
        user.setResetTokenCreatedAt(java.time.LocalDateTime.now());
        when(userRepository.findByResetToken(anyString())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        assertThrows(BadRequestException.class, () -> passwordService.resetPassword("token", "NewPassword1"));
    }

    @Test
    void resetPassword_success_savesUser() {
        User user = new User();
        user.setPassword("encoded");
        user.setResetTokenCreatedAt(java.time.LocalDateTime.now());
        when(userRepository.findByResetToken(anyString())).thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncoded");
        passwordService.resetPassword("token", "NewPassword1");
        verify(userRepository).save(user);
    }
}
