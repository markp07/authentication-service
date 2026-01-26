package nl.markpost.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nl.markpost.authentication.api.v1.model.UserDetails;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.UserRepository;
import nl.markpost.authentication.exception.BadRequestException;
import nl.markpost.authentication.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
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
    user.setId(java.util.UUID.randomUUID());
    user.setUserName("user1");
    user.setEmail("user1@example.com");
    user.set2faEnabled(true);
    when(userRepository.findById(user.getId())).thenReturn(java.util.Optional.of(user));
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
