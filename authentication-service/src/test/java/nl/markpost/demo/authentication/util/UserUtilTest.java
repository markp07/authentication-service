package nl.markpost.demo.authentication.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yubico.webauthn.data.ByteArray;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.common.exception.InternalServerErrorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class UserUtilTest {

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Should return User from security context")
  void getUserFromSecurityContext_success() {
    User user = new User();
    user.setEmail("test@example.com");
    
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(user);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    User result = UserUtil.getUserFromSecurityContext();

    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
  }

  @Test
  @DisplayName("Should throw InternalServerErrorException when principal is not User")
  void getUserFromSecurityContext_notUserPrincipal() {
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn("notAUserObject");
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    assertThrows(InternalServerErrorException.class, () -> UserUtil.getUserFromSecurityContext());
  }

  @Test
  @DisplayName("Should convert User ID to ByteArray correctly")
  void getIdAsByteArray_success() {
    UUID userId = UUID.randomUUID();
    User user = new User();
    user.setId(userId);

    ByteArray result = UserUtil.getIdAsByteArray(user);

    assertNotNull(result);
    String uuidFromBytes = new String(result.getBytes(), StandardCharsets.UTF_8);
    assertEquals(userId.toString(), uuidFromBytes);
  }
}
