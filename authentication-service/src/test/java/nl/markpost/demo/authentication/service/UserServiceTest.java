package nl.markpost.demo.authentication.service;

import nl.markpost.demo.authentication.api.v1.model.UserDetails;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.authentication.service.UserService;
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

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void checkIfUserExists_existingUser_throwsBadRequest() {
        when(userRepository.findByUserName(anyString())).thenReturn(new User());
        assertThrows(BadRequestException.class, () -> userService.checkIfUserExists("existing"));
    }

    @Test
    void checkIfEmailExists_existingEmail_throwsBadRequest() {
        when(userRepository.findByEmail(anyString())).thenReturn(new User());
        assertThrows(BadRequestException.class, () -> userService.checkIfEmailExists("test@example.com"));
    }

    @Test
    void deleteAccount_wrongPassword_throwsUnauthorized() {
        User user = new User();
        user.setPassword("encoded");
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        assertThrows(UnauthorizedException.class, () -> userService.deleteAccount(user, "wrong"));
    }

    @Test
    void getUserDetails_returnsCorrectDetails() {
        User user = new User();
        user.setUserName("user1");
        user.setEmail("user1@example.com");
        user.set2faEnabled(true);
        UserDetails details = userService.getUserDetails(user);
        assertEquals("user1", details.getUserName());
        assertEquals("user1@example.com", details.getEmail());
        assertTrue(details.getTwoFactorEnabled());
    }

    @Test
    void updateUserName_existingUser_throwsBadRequest() {
        User user = new User();
        user.setUserName("oldName");
        doThrow(new BadRequestException()).when(userRepository).findByUserName(anyString());
        assertThrows(BadRequestException.class, () -> userService.updateUserName(user, "existingName"));
    }

    @Test
    void deleteAccount_success_deletesUser() {
        User user = new User();
        user.setPassword("encoded");
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        userService.deleteAccount(user, "password");
        verify(userRepository).delete(user);
    }
}
