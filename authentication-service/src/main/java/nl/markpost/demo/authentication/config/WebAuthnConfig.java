package nl.markpost.demo.authentication.config;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.PasskeyCredentialRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class WebAuthnConfig {

  @Bean
  public CredentialRepository credentialRepository(
      PasskeyCredentialRepository passkeyCredentialRepository,
      nl.markpost.demo.authentication.repository.UserRepository userRepository) {
    return new CredentialRepository() {

      @Override
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

      @Override
      public Optional<ByteArray> getUserHandleForUsername(String username) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
          return Optional.empty();
        }
        // Use UUID as userHandle to match what's used during registration
        String uuid = user.getId().toString();
        ByteArray userHandle = new ByteArray(
            uuid.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        log.info("UserHandle for user " + username + ": " + uuid);

        return Optional.of(userHandle);
      }

      @Override
      public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        // UserHandle contains UUID, need to look up user by UUID
        String uuidStr = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        log.info("[WebAuthnConfig] getUsernameForUserHandle called with: " + uuidStr);
        try {
          UUID userId = UUID.fromString(uuidStr);
          log.info("[WebAuthnConfig] Successfully parsed UUID: " + userId);
          User me = userRepository.findByUserName("Mark");
          if(me != null) {
            log.info("[WebAuthnConfig] Test fetch user 'Mark': {} {}", me.getEmail(), me.getId());
            List<PasskeyCredential> credentials = me.getPasskeyCredentials();
            for(PasskeyCredential cred : credentials) {
              log.info("[WebAuthnConfig]   - Mark's credential: {} {}", cred.getCredentialId(), cred.getPublicKey());
            }
          } else {
            log.warn("[WebAuthnConfig] Test fetch user 'Mark' failed: user not found");
          }
          User user = userRepository.findById(userId).orElse(null);
          if (user != null) {
            log.info("[WebAuthnConfig] Found user: " + user.getEmail());
            return Optional.of(user.getEmail());
          } else {
            log.warn("[WebAuthnConfig] User not found in database for UUID: " + userId);
          }
        } catch (IllegalArgumentException e) {
          log.warn("[WebAuthnConfig] Invalid UUID in userHandle: " + uuidStr, e);
        }
        return Optional.empty();
      }

      @Override
      public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        // UserHandle contains UUID
        String uuidStr = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        log.info(
            "[WebAuthnConfig] lookup called with credentialId: " + credentialId.getBase64Url()
                + ", userHandle: " + uuidStr);
        try {
          UUID userId = UUID.fromString(uuidStr);
          User user = userRepository.findById(userId).orElse(null);
          if (user == null) {
            log.warn("[WebAuthnConfig] User not found for UUID: " + userId);
            return Optional.empty();
          }
          String credentialIdBase64 = credentialId.getBase64Url();
          log.info("[WebAuthnConfig] Looking up credential with ID: " + credentialIdBase64);
          PasskeyCredential cred = passkeyCredentialRepository.findByCredentialId(credentialIdBase64);
          if (cred == null) {
            log.warn(
                "[WebAuthnConfig] Credential not found in database for ID: " + credentialIdBase64);
            // List all credentials for this user to debug
            List<PasskeyCredential> allCreds = passkeyCredentialRepository.findByUserId(userId);
            log.info("[WebAuthnConfig] User has " + allCreds.size() + " credentials:");
            for (PasskeyCredential c : allCreds) {
              log.info("[WebAuthnConfig]   - " + c.getCredentialId());
            }
            return Optional.empty();
          }
          log.info("[WebAuthnConfig] Credential found successfully");

          User me = userRepository.findByUserName("Mark");
          if(me != null) {
            log.info("[WebAuthnConfig] Test fetch user 'Mark': {} {}", me.getEmail(), me.getId());
            List<PasskeyCredential> credentials = me.getPasskeyCredentials();
            for (PasskeyCredential mecred : credentials) {
              log.info("[WebAuthnConfig]   - Mark's credential: {} {}", mecred.getCredentialId(),
                  mecred.getPublicKey());
            }
          }
          return Optional.of(RegisteredCredential.builder()
              .credentialId(credentialId)
              .userHandle(userHandle)
              .publicKeyCose(ByteArray.fromBase64(cred.getPublicKey()))
              .build());
        } catch (IllegalArgumentException e) {
          log.warn("Invalid UUID in userHandle: " + uuidStr);
          return Optional.empty();
        }
      }

      @Override
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
                      .publicKeyCose(ByteArray.fromBase64(cred.getPublicKey()))
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
    };
  }

  @Bean
  public RelyingParty relyingParty(
      @Value("${webauthn.rp.id}") String rpId,
      @Value("${webauthn.rp.name}") String rpName,
      @Value("${webauthn.origin}") String origin,
      CredentialRepository credentialRepository
  ) {
    RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
        .id(rpId)
        .name(rpName)
        .build();
    return RelyingParty.builder()
        .identity(rpIdentity)
        .credentialRepository(credentialRepository)
        .origins(Set.of(origin))
        .allowOriginPort(true)
        .allowOriginSubdomain(true)
        .validateSignatureCounter(true)
        .build();
  }
}
