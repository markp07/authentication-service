package nl.markpost.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import nl.markpost.authentication.model.PasskeyCredential;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.PasskeyCredentialRepository;
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
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    testUser = new User();
    testUser.setId(userId);
    testUser.setEmail("test@example.com");
  }

  @Test
  @DisplayName("Should return empty set when user not found")
  void getCredentialIdsForUsername_userNotFound() {
    when(userService.getUserByEmail("unknown@example.com")).thenReturn(null);

    Set<PublicKeyCredentialDescriptor> result = 
        webAuthnCredentialService.getCredentialIdsForUsername("unknown@example.com");

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should return credential IDs for user")
  void getCredentialIdsForUsername_success() {
    PasskeyCredential credential = PasskeyCredential.builder()
        .credentialId("dGVzdENyZWRlbnRpYWxJZA") // base64url encoded "testCredentialId"
        .user(testUser)
        .build();

    when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);
    when(passkeyCredentialRepository.findByUserId(userId)).thenReturn(List.of(credential));

    Set<PublicKeyCredentialDescriptor> result = 
        webAuthnCredentialService.getCredentialIdsForUsername("test@example.com");

    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Should return empty optional when user not found for handle")
  void getUserHandleForUsername_userNotFound() {
    when(userService.getUserByEmail("unknown@example.com")).thenReturn(null);

    Optional<ByteArray> result = 
        webAuthnCredentialService.getUserHandleForUsername("unknown@example.com");

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should return user handle for username")
  void getUserHandleForUsername_success() {
    when(userService.getUserByEmail("test@example.com")).thenReturn(testUser);

    Optional<ByteArray> result = 
        webAuthnCredentialService.getUserHandleForUsername("test@example.com");

    assertTrue(result.isPresent());
    String uuidFromHandle = new String(result.get().getBytes(), StandardCharsets.UTF_8);
    assertEquals(userId.toString(), uuidFromHandle);
  }

  @Test
  @DisplayName("Should return username for valid user handle")
  void getUsernameForUserHandle_success() {
    ByteArray userHandle = new ByteArray(userId.toString().getBytes(StandardCharsets.UTF_8));
    when(userService.getUserById(userId)).thenReturn(testUser);

    Optional<String> result = webAuthnCredentialService.getUsernameForUserHandle(userHandle);

    assertTrue(result.isPresent());
    assertEquals("test@example.com", result.get());
  }

  @Test
  @DisplayName("Should return empty optional for invalid UUID in lookup")
  void lookup_invalidUuid() {
    ByteArray credentialId = new ByteArray("testCredId".getBytes(StandardCharsets.UTF_8));
    ByteArray userHandle = new ByteArray("not-a-uuid".getBytes(StandardCharsets.UTF_8));

    Optional<RegisteredCredential> result = 
        webAuthnCredentialService.lookup(credentialId, userHandle);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should return empty set for invalid UUID in lookupAll")
  void lookupAll_invalidUuid() {
    ByteArray userHandle = new ByteArray("not-a-uuid".getBytes(StandardCharsets.UTF_8));

    Set<RegisteredCredential> result = webAuthnCredentialService.lookupAll(userHandle);

    assertTrue(result.isEmpty());
  }

  @Test
  @DisplayName("Should return all credentials for user handle")
  void lookupAll_success() {
    ByteArray userHandle = new ByteArray(userId.toString().getBytes(StandardCharsets.UTF_8));
    
    PasskeyCredential credential = PasskeyCredential.builder()
        .credentialId("dGVzdENyZWRlbnRpYWxJZA") // base64url
        .publicKey("dGVzdFB1YmxpY0tleQ") // base64url for "testPublicKey"
        .user(testUser)
        .build();

    when(userService.getUserById(userId)).thenReturn(testUser);
    when(passkeyCredentialRepository.findByUserId(userId)).thenReturn(List.of(credential));

    Set<RegisteredCredential> result = webAuthnCredentialService.lookupAll(userHandle);

    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Should return empty set when no credentials for user")
  void lookupAll_noCredentials() {
    ByteArray userHandle = new ByteArray(userId.toString().getBytes(StandardCharsets.UTF_8));

    when(userService.getUserById(userId)).thenReturn(testUser);
    when(passkeyCredentialRepository.findByUserId(userId)).thenReturn(List.of());

    Set<RegisteredCredential> result = webAuthnCredentialService.lookupAll(userHandle);

    assertTrue(result.isEmpty());
  }
}
