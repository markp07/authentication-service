package nl.markpost.demo.authentication.service;

import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.PasskeyCredentialRepository;
import nl.markpost.demo.common.exception.UnauthorizedException;
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
    try {
      UUID userId = parseUuidFromBytes(userHandle.getBytes());
      User user = userService.getUserById(userId);
      return Optional.of(user.getEmail());
    } catch (IllegalArgumentException e) {
      log.warn("Invalid UUID in userHandle", e);
      return Optional.empty();
    }
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
    try {
      UUID userId = parseUuidFromBytes(userHandle.getBytes());
      userService.getUserById(userId);
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
      log.error("[WebAuthnCredentialService] Invalid UUID in userHandle", e);
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
    try {
      UUID userId = parseUuidFromBytes(userHandle.getBytes());
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
      log.warn("Invalid UUID in userHandle", e);
      return Set.of();
    }
  }

  /**
   * Parses a UUID from bytes, handling UTF-8 encoded UUID strings.
   * This method prioritizes UTF-8 string parsing since that's the format used
   * during passkey registration.
   *
   * @param bytes the byte array containing the UUID
   * @return the parsed UUID
   * @throws IllegalArgumentException if the bytes cannot be parsed as a UUID
   */
  private UUID parseUuidFromBytes(byte[] bytes) {
    // Try to parse as UTF-8 string (standard format used during registration)
    String uuidStr = new String(bytes, StandardCharsets.UTF_8);
    try {
      return UUID.fromString(uuidStr);
    } catch (IllegalArgumentException e) {
      // Log the failure with byte length for debugging
      throw new IllegalArgumentException(
          "Unable to parse UUID from bytes: invalid format (length=" + bytes.length + ")", e);
    }
  }
}

