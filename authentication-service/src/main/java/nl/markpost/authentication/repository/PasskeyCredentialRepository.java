package nl.markpost.authentication.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.markpost.authentication.model.PasskeyCredential;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for managing PasskeyCredential entities.
 */
public interface PasskeyCredentialRepository extends JpaRepository<PasskeyCredential, UUID> {

  /**
   * Finds all PasskeyCredentials associated with a specific user ID.
   *
   * @param userId the UUID of the user
   * @return a list of PasskeyCredentials
   */
  List<PasskeyCredential> findByUserId(UUID userId);

  /**
   * Finds a PasskeyCredential by its credential ID.
   *
   * @param credentialId the credential ID
   * @return the PasskeyCredential
   */
  Optional<PasskeyCredential> findByCredentialId(String credentialId);

  /**
   * Finds a PasskeyCredential by its credential ID and user ID.
   *
   * @param credentialId the credential ID
   * @param userId       the UUID of the user
   * @return an Optional containing the PasskeyCredential if found, or empty if not found
   */
  Optional<PasskeyCredential> findByCredentialIdAndUserId(String credentialId, UUID userId);

}

