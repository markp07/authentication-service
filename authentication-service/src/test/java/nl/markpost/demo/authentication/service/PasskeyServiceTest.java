package nl.markpost.demo.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yubico.webauthn.RelyingParty;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import nl.markpost.demo.authentication.api.v1.model.PasskeyInfoDto;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.PasskeyCredentialRepository;
import nl.markpost.demo.authentication.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PasskeyServiceTest {

  private PasskeyService passkeyService;
  private PasskeyCredentialRepository passkeyCredentialRepository;
  private UserRepository userRepository;
  private RelyingParty relyingParty;
  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    passkeyCredentialRepository = mock(PasskeyCredentialRepository.class);
    userRepository = mock(UserRepository.class);
    relyingParty = mock(RelyingParty.class);
    jwtService = mock(JwtService.class);
    
    passkeyService = new PasskeyService(
        passkeyCredentialRepository,
        userRepository,
        relyingParty,
        jwtService
    );
  }

  @Test
  @DisplayName("Should list passkeys for user")
  void listPasskeys_success() {
    User user = new User();
    user.setId(UUID.randomUUID());
    
    PasskeyCredential cred1 = new PasskeyCredential();
    cred1.setCredentialId("cred1");
    cred1.setName("Passkey 1");
    cred1.setCreatedAt(LocalDateTime.now());
    
    PasskeyCredential cred2 = new PasskeyCredential();
    cred2.setCredentialId("cred2");
    cred2.setName("Passkey 2");
    cred2.setCreatedAt(LocalDateTime.now());
    
    when(passkeyCredentialRepository.findByUserId(user.getId()))
        .thenReturn(Arrays.asList(cred1, cred2));
    
    List<PasskeyInfoDto> result = passkeyService.listPasskeys(user);
    
    assertEquals(2, result.size());
    assertEquals("cred1", result.get(0).getCredentialId());
    assertEquals("Passkey 1", result.get(0).getName());
    assertEquals("cred2", result.get(1).getCredentialId());
    assertEquals("Passkey 2", result.get(1).getName());
  }

  @Test
  @DisplayName("Should return empty list when user is null")
  void listPasskeys_nullUser() {
    List<PasskeyInfoDto> result = passkeyService.listPasskeys(null);
    
    assertTrue(result.isEmpty());
    verify(passkeyCredentialRepository, never()).findByUserId(any());
  }

  @Test
  @DisplayName("Should delete passkey when user owns it")
  void deletePasskey_success() {
    User user = new User();
    user.setId(UUID.randomUUID());
    
    PasskeyCredential cred = new PasskeyCredential();
    cred.setCredentialId("cred1");
    cred.setUser(user);
    
    when(passkeyCredentialRepository.findByCredentialId("cred1")).thenReturn(cred);
    
    passkeyService.deletePasskey(user, "cred1");
    
    verify(passkeyCredentialRepository).delete(cred);
  }

  @Test
  @DisplayName("Should not delete passkey when user is null")
  void deletePasskey_nullUser() {
    passkeyService.deletePasskey(null, "cred1");
    
    verify(passkeyCredentialRepository, never()).findByCredentialId(any());
    verify(passkeyCredentialRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should not delete passkey when credential not found")
  void deletePasskey_credentialNotFound() {
    User user = new User();
    user.setId(UUID.randomUUID());
    
    when(passkeyCredentialRepository.findByCredentialId("cred1")).thenReturn(null);
    
    passkeyService.deletePasskey(user, "cred1");
    
    verify(passkeyCredentialRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should not delete passkey when user does not own it")
  void deletePasskey_wrongOwner() {
    User user = new User();
    user.setId(UUID.randomUUID());
    
    User otherUser = new User();
    otherUser.setId(UUID.randomUUID());
    
    PasskeyCredential cred = new PasskeyCredential();
    cred.setCredentialId("cred1");
    cred.setUser(otherUser);
    
    when(passkeyCredentialRepository.findByCredentialId("cred1")).thenReturn(cred);
    
    passkeyService.deletePasskey(user, "cred1");
    
    verify(passkeyCredentialRepository, never()).delete(any());
  }
}
