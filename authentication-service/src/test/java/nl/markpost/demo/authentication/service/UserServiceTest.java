package nl.markpost.demo.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nl.markpost.demo.authentication.api.v1.model.UserDetails;
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
