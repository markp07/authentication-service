package nl.markpost.demo.weather.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.markpost.demo.weather.entity.SavedLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for SavedLocation entity.
 */
@Repository
public interface SavedLocationRepository extends JpaRepository<SavedLocation, Long> {

  /**
   * Find all saved locations for a user, ordered by display order.
   *
   * @param userId the user ID
   * @return list of saved locations
   */
  List<SavedLocation> findByUserIdOrderByDisplayOrderAsc(UUID userId);

  /**
   * Find a saved location by user ID and location ID.
   *
   * @param userId     the user ID
   * @param locationId the location ID
   * @return optional saved location
   */
  Optional<SavedLocation> findByUserIdAndLocationId(UUID userId, Long locationId);

  /**
   * Delete a saved location by location ID and user ID.
   *
   * @param locationId the location ID (external geocoding ID)
   * @param userId     the user ID
   */
  void deleteByLocationIdAndUserId(Long locationId, UUID userId);
}
