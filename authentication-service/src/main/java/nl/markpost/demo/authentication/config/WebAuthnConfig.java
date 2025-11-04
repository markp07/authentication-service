package nl.markpost.demo.authentication.config;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.PasskeyCredentialRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebAuthnConfig {

  @Bean
  public CredentialRepository credentialRepository(
      PasskeyCredentialRepository passkeyCredentialRepository,
      nl.markpost.demo.authentication.repository.UserRepository userRepository) {
    return new CredentialRepository() {

      Logger logger = Logger.getLogger(WebAuthnConfig.class.getName());

      @Override
      public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
          return Set.of();
        }
        return passkeyCredentialRepository.findByUserId(user.getId()).stream()
            .map(cred -> PublicKeyCredentialDescriptor.builder()
                .id(new ByteArray(Base64.getUrlDecoder().decode(cred.getCredentialId())))
                .build())
            .collect(java.util.stream.Collectors.toSet());
      }

      @Override
      public Optional<ByteArray> getUserHandleForUsername(String username) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
          return Optional.empty();
        }
        // Use UUID as userHandle to match what's used during registration
        String uuid = user.getId().toString();
        ByteArray userHandle = new ByteArray(uuid.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        logger.info("UserHandle for user " + username + ": " + uuid);

        return Optional.of(userHandle);
      }

      @Override
      public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        // UserHandle contains UUID, need to look up user by UUID
        String uuidStr = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        try {
          UUID userId = UUID.fromString(uuidStr);
          User user = userRepository.findById(userId).orElse(null);
          if (user != null) {
            return Optional.of(user.getEmail());
          }
        } catch (IllegalArgumentException e) {
          logger.warning("Invalid UUID in userHandle: " + uuidStr);
        }
        return Optional.empty();
      }

      private String toBase64UrlNoPadding(ByteArray byteArray) {
        String base64url = Base64.getUrlEncoder().encodeToString(byteArray.getBytes());
        return base64url.replaceAll("=+$", "");
      }

      @Override
      public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        // UserHandle contains UUID
        String uuidStr = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        try {
          UUID userId = UUID.fromString(uuidStr);
          User user = userRepository.findById(userId).orElse(null);
          if (user == null) {
            return Optional.empty();
          }
          String base64urlNoPad = toBase64UrlNoPadding(credentialId);
          PasskeyCredential cred = passkeyCredentialRepository.findByCredentialId(base64urlNoPad);
          if (cred == null) {
            return Optional.empty();
          }
          return Optional.of(RegisteredCredential.builder()
              .credentialId(credentialId)
              .userHandle(userHandle)
              .publicKeyCose(ByteArray.fromBase64(cred.getPublicKey()))
              .build());
        } catch (IllegalArgumentException e) {
          logger.warning("Invalid UUID in userHandle: " + uuidStr);
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
              .map(cred -> RegisteredCredential.builder()
                  .credentialId(new ByteArray(Base64.getUrlDecoder().decode(cred.getCredentialId())))
                  .userHandle(userHandle)
                  .publicKeyCose(ByteArray.fromBase64(cred.getPublicKey()))
                  .build())
              .collect(java.util.stream.Collectors.toSet());
        } catch (IllegalArgumentException e) {
          logger.warning("Invalid UUID in userHandle: " + uuidStr);
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
