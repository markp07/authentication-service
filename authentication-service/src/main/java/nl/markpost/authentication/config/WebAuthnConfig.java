package nl.markpost.authentication.config;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.service.WebAuthnCredentialService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for WebAuthn.
 */
@Slf4j
@Configuration
public class WebAuthnConfig {

  /**
   * Bean for CredentialRepository that delegates to WebAuthnCredentialService.
   *
   * @param credentialService the WebAuthnCredentialService
   * @return the CredentialRepository bean
   */
  @Bean
  public CredentialRepository credentialRepository(WebAuthnCredentialService credentialService) {
    return new CredentialRepository() {

      @Override
      public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        return credentialService.getCredentialIdsForUsername(username);
      }

      @Override
      public Optional<ByteArray> getUserHandleForUsername(String username) {
        return credentialService.getUserHandleForUsername(username);
      }

      @Override
      public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        return credentialService.getUsernameForUserHandle(userHandle);
      }

      @Override
      public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        return credentialService.lookup(credentialId, userHandle);
      }

      @Override
      public Set<RegisteredCredential> lookupAll(ByteArray userHandle) {
        return credentialService.lookupAll(userHandle);
      }
    };
  }

  /**
   * Bean for RelyingParty configured with application properties.
   *
   * @param rpId                 the Relying Party ID
   * @param rpName               the Relying Party name
   * @param origin               the origin URL
   * @param credentialRepository the CredentialRepository
   * @return the RelyingParty bean
   */
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
