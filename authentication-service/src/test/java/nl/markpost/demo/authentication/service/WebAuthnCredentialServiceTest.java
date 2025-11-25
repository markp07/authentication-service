package nl.markpost.demo.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.yubico.webauthn.data.ByteArray;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.PasskeyCredentialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebAuthnCredentialServiceTest {

  @Mock
  private PasskeyCredentialRepository passkeyCredentialRepository;

  @Mock
  private UserService userService;

  @InjectMocks
  private WebAuthnCredentialService webAuthnCredentialService;

  private User testUser;
  private UUID testUserId;

  @BeforeEach
  void setUp() {
    testUserId = UUID.randomUUID();
    testUser = new User();
    testUser.setId(testUserId);
    testUser.setEmail("test@example.com");
    testUser.setUserName("testuser");
  }

  @Test
  @DisplayName("Should get username for UTF-8 encoded UUID userHandle")
  void getUsernameForUserHandle_utf8EncodedUuid() {
    // Create a UTF-8 encoded UUID string (36 bytes)
    String uuidString = testUserId.toString();
    byte[] utf8Bytes = uuidString.getBytes(StandardCharsets.UTF_8);
    ByteArray userHandle = new ByteArray(utf8Bytes);

    when(userService.getUserById(testUserId)).thenReturn(testUser);

    Optional<String> result = webAuthnCredentialService.getUsernameForUserHandle(userHandle);

    assertTrue(result.isPresent());
    assertEquals("test@example.com", result.get());
  }

  @Test
  @DisplayName("Should return empty for invalid userHandle")
  void getUsernameForUserHandle_invalidUserHandle() {
    // Create an invalid byte array that cannot be parsed as UUID
    byte[] invalidBytes = new byte[]{1, 2, 3, 4, 5};
    ByteArray userHandle = new ByteArray(invalidBytes);

    Optional<String> result = webAuthnCredentialService.getUsernameForUserHandle(userHandle);

    assertFalse(result.isPresent());
  }

  @Test
  @DisplayName("Should return empty for lookupAll with invalid userHandle")
  void lookupAll_invalidUserHandle() {
    // Create an invalid byte array that cannot be parsed as UUID
    byte[] invalidBytes = new byte[]{1, 2, 3, 4, 5};
    ByteArray userHandle = new ByteArray(invalidBytes);

    var result = webAuthnCredentialService.lookupAll(userHandle);

    assertTrue(result.isEmpty());
  }
}
