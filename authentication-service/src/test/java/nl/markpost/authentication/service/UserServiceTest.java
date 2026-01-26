package nl.markpost.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import nl.markpost.authentication.api.v1.model.UserDetails;
import nl.markpost.authentication.exception.BadRequestException;
import nl.markpost.authentication.exception.UnauthorizedException;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  @Test
  void deleteAccount_wrongPassword_throwsUnauthorized() {
    User user = User.builder()
        .id(UUID.randomUUID())
        .userName("user1")
        .email("user1@example.com")
        .password("encodedPassword")
        .build();
    when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);
    assertThrows(UnauthorizedException.class, () -> userService.deleteAccount(user, "wrongPassword"));
  }

  @Test
  void getUserDetails_returnsCorrectDetails() {
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .id(userId)
        .userName("user1")
        .email("user1@example.com")
        .is2faEnabled(true)
        .emailVerified(true)
        .createdAt(LocalDateTime.now())
        .passkeyCredentials(java.util.Collections.emptyList())
        .build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    UserDetails details = userService.getUserDetails(user);

    assertEquals("user1", details.getUserName());
    assertEquals("user1@example.com", details.getEmail());
    assertTrue(details.getTwoFactorEnabled());
  }

  @Test
  void updateUserName_existingUser_throwsBadRequest() {
    User user = User.builder()
        .id(UUID.randomUUID())
        .userName("oldName")
        .email("test@example.com")
        .password("password")
        .build();

    User existingUser = User.builder()
        .id(UUID.randomUUID())
        .userName("existingName")
        .email("existing@example.com")
        .password("password")
        .build();

    when(userRepository.findByUserName("existingName")).thenReturn(Optional.of(existingUser));

    assertThrows(BadRequestException.class, () -> userService.updateUserName(user, "existingName"));

    verify(userRepository).findByUserName("existingName");
  }

  @Test
  void deleteAccount_success_deletesUser() {
    User user = User.builder()
        .id(UUID.randomUUID())
        .userName("user1")
        .email("user1@example.com")
        .password("encodedPassword")
        .build();

    when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
    userService.deleteAccount(user, "password");
    verify(userRepository).delete(user);
  }
}
