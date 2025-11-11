package nl.markpost.demo.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private DatabaseUserDetailsService service;

  @Test
  @DisplayName("Should return user details when user exists")
  void loadUserByUsername_userExists() {
    User user = new User();
    user.setEmail("user@example.com");
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    UserDetails result = service.loadUserByUsername("user@example.com");
    assertEquals(user, result);
  }
}

