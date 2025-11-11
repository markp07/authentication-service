package nl.markpost.demo.authentication.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, UUID> {

  List<PasskeyCredential> findByUserId(UUID userId);

  PasskeyCredential findByCredentialId(String credentialId);

  Optional<PasskeyCredential> findByCredentialIdAndUserId(String credentialId, UUID userId);

}

