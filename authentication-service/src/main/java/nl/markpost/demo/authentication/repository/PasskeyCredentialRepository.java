package nl.markpost.demo.authentication.repository;

import java.util.List;
import java.util.UUID;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import nl.markpost.demo.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, UUID> {

  List<PasskeyCredential> findByUserId(UUID userId);

  PasskeyCredential findByCredentialId(String credentialId);

  void deleteByCredentialId(String credentialId);
}

