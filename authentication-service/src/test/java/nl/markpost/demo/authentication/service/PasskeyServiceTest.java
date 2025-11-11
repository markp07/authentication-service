package nl.markpost.demo.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.markpost.demo.authentication.api.v1.model.PasskeyInfoDto;
import nl.markpost.demo.authentication.api.v1.model.PublicKeyCredentialRequestOptionsDto;
import nl.markpost.demo.authentication.mapper.PasskeyCredentialMapper;
import nl.markpost.demo.authentication.mapper.PasskeyInfoDtoMapper;
import nl.markpost.demo.authentication.mapper.StartRegistrationOptionsMapper;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.PasskeyCredentialRepository;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.common.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class PasskeyServiceTest {

  @Mock
  private PasskeyCredentialRepository passkeyCredentialRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private RelyingParty relyingParty;
  @Mock
  private JwtService jwtService;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private PasskeyInfoDtoMapper passkeyInfoDtoMapper;
  @Mock
  private StartRegistrationOptionsMapper startRegistrationOptionsMapper;
  @Mock
  private PasskeyCredentialMapper passkeyCredentialMapper;

  @InjectMocks
  private PasskeyService passkeyService;
  private MockHttpSession session;

  @BeforeEach
  void setUp() {
    // Setup mock request context
    session = new MockHttpSession();
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setSession(session);
    MockHttpServletResponse response = new MockHttpServletResponse();

    ServletRequestAttributes attributes = new ServletRequestAttributes(request, response);
    RequestContextHolder.setRequestAttributes(attributes);
  }

  @Test
  @DisplayName("Should list passkeys for user")
  void listPasskeys_success() {
    User user = createUser();

    PasskeyCredential cred1 = createPasskeyCredential("cred1", "Passkey 1");
    PasskeyCredential cred2 = createPasskeyCredential("cred2", "Passkey 2");

    PasskeyInfoDto dto1 = new PasskeyInfoDto();
    dto1.setCredentialId("cred1");
    dto1.setName("Passkey 1");

    PasskeyInfoDto dto2 = new PasskeyInfoDto();
    dto2.setCredentialId("cred2");
    dto2.setName("Passkey 2");

    when(passkeyCredentialRepository.findByUserId(user.getId()))
        .thenReturn(Arrays.asList(cred1, cred2));
    when(passkeyInfoDtoMapper.from(cred1)).thenReturn(dto1);
    when(passkeyInfoDtoMapper.from(cred2)).thenReturn(dto2);

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
    User user = createUser();
    setupSecurityContext(user);

    PasskeyCredential cred = createPasskeyCredential("cred1", "Passkey 1");
    cred.setUser(user);

    when(passkeyCredentialRepository.findByCredentialIdAndUserId("cred1", user.getId()))
        .thenReturn(Optional.of(cred));

    passkeyService.deletePasskey("cred1");

    verify(passkeyCredentialRepository).delete(cred);
  }

  @Test
  @DisplayName("Should throw NotFoundException when credential not found")
  void deletePasskey_credentialNotFound() {
    User user = createUser();
    setupSecurityContext(user);

    when(passkeyCredentialRepository.findByCredentialIdAndUserId("cred1", user.getId()))
        .thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> passkeyService.deletePasskey("cred1"));

    verify(passkeyCredentialRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Should start usernameless authentication and return options")
  void startUsernamelessAuthentication_success() {
    // Use real ByteArray instead of mock since it has final methods
    ByteArray challenge = new ByteArray(new byte[]{1, 2, 3});

    AssertionRequest assertionRequest = mock(AssertionRequest.class);
    PublicKeyCredentialRequestOptions requestOptions = mock(
        PublicKeyCredentialRequestOptions.class);

    when(relyingParty.startAssertion(any(StartAssertionOptions.class))).thenReturn(
        assertionRequest);
    when(assertionRequest.getPublicKeyCredentialRequestOptions()).thenReturn(requestOptions);
    when(requestOptions.getChallenge()).thenReturn(challenge);
    when(requestOptions.getTimeout()).thenReturn(java.util.Optional.empty());
    when(requestOptions.getAllowCredentials()).thenReturn(java.util.Optional.empty());
    when(requestOptions.getUserVerification()).thenReturn(java.util.Optional.empty());

    PublicKeyCredentialRequestOptionsDto result = passkeyService.startUsernamelessAuthentication();

    assertNotNull(result);
    verify(relyingParty).startAssertion(any(StartAssertionOptions.class));
    assertEquals(assertionRequest, session.getAttribute("webauthn_usernameless_assertion_request"));
  }

  private User createUser() {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail("test@example.com");
    user.setUserName("testuser");
    return user;
  }

  private PasskeyCredential createPasskeyCredential(String credentialId, String name) {
    PasskeyCredential cred = new PasskeyCredential();
    cred.setId(UUID.randomUUID());
    cred.setCredentialId(credentialId);
    cred.setName(name);
    cred.setCreatedAt(LocalDateTime.now());
    return cred;
  }

  private void setupSecurityContext(User user) {
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(user);
    SecurityContextHolder.setContext(securityContext);
  }
}
