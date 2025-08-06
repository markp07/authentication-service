package nl.markpost.demo.authentication.config;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialType;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.PasskeyCredentialRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;
import java.util.Optional;
import java.util.Set;

@Configuration
public class WebAuthnConfig {

    @Bean
    public CredentialRepository credentialRepository(PasskeyCredentialRepository passkeyCredentialRepository, nl.markpost.demo.authentication.repository.UserRepository userRepository) {
        return new CredentialRepository() {
            @Override
            public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
                User user = userRepository.findByEmail(username);
                if (user == null) return Set.of();
                return passkeyCredentialRepository.findByUser(user).stream()
                        .map(cred -> PublicKeyCredentialDescriptor.builder()
                                .id(new ByteArray(Base64.getUrlDecoder().decode(cred.getCredentialId())))
                                .build())
                        .collect(java.util.stream.Collectors.toSet());
            }

            @Override
            public Optional<ByteArray> getUserHandleForUsername(String username) {
                User user = userRepository.findByEmail(username);
                if (user == null) return Optional.empty();
                String encodedEmail = Base64.getEncoder().encodeToString(user.getEmail().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                return Optional.of(ByteArray.fromBase64(encodedEmail));
            }

            @Override
            public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
                String email = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return Optional.of(email);
            }

            @Override
            public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
                String email = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
                User user = userRepository.findByEmail(email);
                if (user == null) return Optional.empty();
                PasskeyCredential cred = passkeyCredentialRepository.findByCredentialId(Base64.getUrlEncoder().encodeToString(credentialId.getBytes()));
                if (cred == null) return Optional.empty();
                return Optional.of(RegisteredCredential.builder()
                        .credentialId(credentialId)
                        .userHandle(userHandle)
                        .publicKeyCose(ByteArray.fromBase64(cred.getPublicKey()))
                        .build());
            }

            @Override
            public Set<RegisteredCredential> lookupAll(ByteArray userHandle) {
                String email = new String(userHandle.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
                User user = userRepository.findByEmail(email);
                if (user == null) return Set.of();
                return passkeyCredentialRepository.findByUser(user).stream()
                        .map(cred -> RegisteredCredential.builder()
                                .credentialId(new ByteArray(Base64.getUrlDecoder().decode(cred.getCredentialId())))
                                .userHandle(userHandle)
                                .publicKeyCose(ByteArray.fromBase64(cred.getPublicKey()))
                                .build())
                        .collect(java.util.stream.Collectors.toSet());
            }
        };
    }

    @Bean
    public RelyingParty relyingParty(
            @Value("${webauthn.rp.id:localhost}") String rpId,
            @Value("${webauthn.rp.name:Demo Authentication}") String rpName,
            @Value("${webauthn.origin:http://localhost:12002}") String origin,
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
                .build();
    }
}
