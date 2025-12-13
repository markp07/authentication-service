package nl.markpost.authentication.service;

import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.exception.UnauthorizedException;
import nl.markpost.authentication.model.PasskeyCredential;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.PasskeyCredentialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing WebAuthn credentials.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebAuthnCredentialService {

  private final PasskeyCredentialRepository passkeyCredentialRepository;
  private final UserService userService;

  /**
   * Retrieves the credential IDs associated with a given username.
   *
   * @param username the username (email) of the user
   * @return a set of PublicKeyCredentialDescriptor objects
   */
  @Transactional(readOnly = true)
  public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
    User user = userService.getUserByEmail(username);
    if (user == null) {
      return Set.of();
    }
    return passkeyCredentialRepository.findByUserId(user.getId()).stream()
        .map(cred -> {
          try {
            return PublicKeyCredentialDescriptor.builder()
                .id(ByteArray.fromBase64Url(cred.getCredentialId()))
                .build();
          } catch (Exception e) {
            log.error("Failed to decode credential ID: " + cred.getCredentialId(), e);
            throw new RuntimeException(e);
          }
        })
        .collect(Collectors.toSet());
  }

  /**
   * Retrieves the user handle for a given username.
   *
   * @param username the username (email) of the user
   * @return an Optional containing the user handle as ByteArray, or empty if not found
   */
  @Transactional(readOnly = true)
  public Optional<ByteArray> getUserHandleForUsername(String username) {
    User user = userService.getUserByEmail(username);
    if (user == null) {
      return Optional.empty();
    }
    // Use UUID as userHandle to match what's used during registration
    String uuid = user.getId().toString();
    ByteArray userHandle = new ByteArray(uuid.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    return Optional.of(userHandle);
  }

  /**
   * Retrieves the username associated with a given user handle.
   *
   * @param userHandle the user handle as ByteArray
   * @return an Optional containing the username, or empty if not found
   */
  @Transactional(readOnly = true)
  public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
    String uuidStr = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    UUID userId = UUID.fromString(uuidStr);
    User user = userService.getUserById(userId);
    return Optional.of(user.getEmail());
  }

  /**
   * Looks up a registered credential by its credential ID and user handle.
   *
   * @param credentialId the credential ID as ByteArray
   * @param userHandle   the user handle as ByteArray
   * @return an Optional containing the RegisteredCredential, or empty if not found
   */
  @Transactional(readOnly = true)
  public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
    String uuidStr = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    try {
      userService.getUserById(UUID.fromString(uuidStr));
      String credentialIdBase64 = credentialId.getBase64Url();
      PasskeyCredential cred = passkeyCredentialRepository.findByCredentialId(credentialIdBase64)
          .orElseThrow(
              UnauthorizedException::new);
      try {
        return Optional.of(RegisteredCredential.builder()
            .credentialId(credentialId)
            .userHandle(userHandle)
            .publicKeyCose(ByteArray.fromBase64Url(cred.getPublicKey()))
            .build());
      } catch (Exception e) {
        log.error(
            "[WebAuthnCredentialService] Error building RegisteredCredential for credentialId: "
                + credentialIdBase64 + ", publicKey: " + cred.getPublicKey(), e);
        throw new RuntimeException("Failed to build registered credential", e);
      }
    } catch (IllegalArgumentException e) {
      log.error("[WebAuthnCredentialService] Invalid UUID in userHandle: " + uuidStr, e);
      return Optional.empty();
    }
  }

  /**
   * Looks up all registered credentials for a given user handle.
   *
   * @param userHandle the user handle as ByteArray
   * @return a set of RegisteredCredential objects
   */
  @Transactional(readOnly = true)
  public Set<RegisteredCredential> lookupAll(ByteArray userHandle) {
    // UserHandle contains UUID
    String uuidStr = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    try {
      UUID userId = UUID.fromString(uuidStr);
      User user = userService.getUserById(userId);
      return passkeyCredentialRepository.findByUserId(user.getId()).stream()
          .map(cred -> {
            try {
              return RegisteredCredential.builder()
                  .credentialId(ByteArray.fromBase64Url(cred.getCredentialId()))
                  .userHandle(userHandle)
                  .publicKeyCose(ByteArray.fromBase64Url(cred.getPublicKey()))
                  .build();
            } catch (Exception e) {
              log.error("Failed to decode credential: " + cred.getCredentialId(), e);
              throw new RuntimeException(e);
            }
          })
          .collect(Collectors.toSet());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid UUID in userHandle: " + uuidStr);
      return Set.of();
    }
  }
}

