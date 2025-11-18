package nl.markpost.demo.weather.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.weather.api.v1.model.Location;
import nl.markpost.demo.weather.entity.SavedLocation;
import nl.markpost.demo.weather.mapper.SavedLocationMapper;
import nl.markpost.demo.weather.repository.SavedLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing saved locations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SavedLocationService {

  private final SavedLocationRepository savedLocationRepository;
  private final SavedLocationMapper savedLocationMapper;

  /**
   * Get all saved locations for a user.
   *
   * @param userId the user ID
   * @return list of saved locations
   */
  public List<Location> getSavedLocations(UUID userId) {
    log.info("Getting saved locations for user: {}", userId);
    return savedLocationRepository.findByUserId(userId).stream()
        .map(savedLocationMapper::toApiModel)
        .collect(Collectors.toList());
  }

  /**
   * Save a location for a user.
   *
   * @param userId   the user ID
   * @param location the location to save
   * @return the saved location
   */
  @Transactional
  public Location saveLocation(UUID userId, Location location) {
    log.info("Saving location {} for user: {}", location.getName(), userId);

    // Check if location already exists for this user
    if (savedLocationRepository.findByUserIdAndLocationId(userId, location.getId()).isPresent()) {
      log.info("Location {} already saved for user: {}", location.getName(), userId);
      return location;
    }

    SavedLocation savedLocation = savedLocationMapper.toEntity(location);
    savedLocation.setUserId(userId);
    savedLocationRepository.save(savedLocation);
    return location;
  }

  /**
   * Delete a saved location.
   *
   * @param id     the saved location ID
   * @param userId the user ID
   */
  @Transactional
  public void deleteSavedLocation(Long id, UUID userId) {
    log.info("Deleting saved location {} for user: {}", id, userId);
    savedLocationRepository.deleteByIdAndUserId(id, userId);
  }
}
