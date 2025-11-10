package nl.markpost.demo.authentication.service;

import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.PasskeyCredentialRepository;
import nl.markpost.demo.authentication.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebAuthnCredentialService {

  private final PasskeyCredentialRepository passkeyCredentialRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
    User user = userRepository.findByEmail(username);
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

  @Transactional(readOnly = true)
  public Optional<ByteArray> getUserHandleForUsername(String username) {
    User user = userRepository.findByEmail(username);
    if (user == null) {
      return Optional.empty();
    }
    // Use UUID as userHandle to match what's used during registration
    String uuid = user.getId().toString();
    ByteArray userHandle = new ByteArray(uuid.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    return Optional.of(userHandle);
  }

  @Transactional(readOnly = true)
  public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
    String uuidStr = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    try {
      UUID userId = UUID.fromString(uuidStr);
      User user = userRepository.findById(userId).orElse(null);
      if (user != null) {
        return Optional.of(user.getEmail());
      } else {
        log.warn("[WebAuthnCredentialService] User not found in database for UUID: " + userId);
      }
    } catch (Exception e) {
      log.error("[WebAuthnCredentialService] Error in getUsernameForUserHandle for UUID: " + uuidStr, e);
    }
    return Optional.empty();
  }

  @Transactional(readOnly = true)
  public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
    String uuidStr = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    try {
      UUID userId = UUID.fromString(uuidStr);
      User user = userRepository.findById(userId).orElse(null);
      if (user == null) {
        log.warn("[WebAuthnCredentialService] User not found for UUID: " + userId);
        return Optional.empty();
      }
      String credentialIdBase64 = credentialId.getBase64Url();
      PasskeyCredential cred = passkeyCredentialRepository.findByCredentialId(credentialIdBase64);
      if (cred == null) {
        log.warn("[WebAuthnCredentialService] Credential not found in database for ID: " + credentialIdBase64);
        return Optional.empty();
      }
      try {
        return Optional.of(RegisteredCredential.builder()
            .credentialId(credentialId)
            .userHandle(userHandle)
            .publicKeyCose(ByteArray.fromBase64Url(cred.getPublicKey()))
            .build());
      } catch (Exception e) {
        log.error("[WebAuthnCredentialService] Error building RegisteredCredential for credentialId: "
            + credentialIdBase64 + ", publicKey: " + cred.getPublicKey(), e);
        throw new RuntimeException("Failed to build registered credential", e);
      }
    } catch (IllegalArgumentException e) {
      log.error("[WebAuthnCredentialService] Invalid UUID in userHandle: " + uuidStr, e);
      return Optional.empty();
    }
  }

  @Transactional(readOnly = true)
  public Set<RegisteredCredential> lookupAll(ByteArray userHandle) {
    // UserHandle contains UUID
    String uuidStr = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
    try {
      UUID userId = UUID.fromString(uuidStr);
      User user = userRepository.findById(userId).orElse(null);
      if (user == null) {
        return Set.of();
      }
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

