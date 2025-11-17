package nl.markpost.demo.weather.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.weather.entity.SavedLocation;
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

  /**
   * Get all saved locations for a user.
   *
   * @param userId the user ID
   * @return list of saved locations
   */
  public List<nl.markpost.demo.weather.api.v1.model.Location> getSavedLocations(UUID userId) {
    log.info("Getting saved locations for user: {}", userId);
    return savedLocationRepository.findByUserId(userId).stream()
        .map(this::toLocationDto)
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
  public nl.markpost.demo.weather.api.v1.model.Location saveLocation(UUID userId,
      nl.markpost.demo.weather.api.v1.model.Location location) {
    log.info("Saving location {} for user: {}", location.getName(), userId);

    // Check if location already exists for this user
    if (savedLocationRepository.findByUserIdAndLocationId(userId, location.getId()).isPresent()) {
      log.info("Location {} already saved for user: {}", location.getName(), userId);
      return location;
    }

    SavedLocation savedLocation = SavedLocation.builder()
        .userId(userId)
        .locationId(location.getId())
        .name(location.getName())
        .latitude(location.getLatitude())
        .longitude(location.getLongitude())
        .country(location.getCountry())
        .countryCode(location.getCountryCode())
        .admin1(location.getAdmin1())
        .timezone(location.getTimezone())
        .build();

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

  /**
   * Convert SavedLocation entity to Location DTO.
   *
   * @param savedLocation the saved location entity
   * @return location DTO
   */
  private nl.markpost.demo.weather.api.v1.model.Location toLocationDto(
      SavedLocation savedLocation) {
    nl.markpost.demo.weather.api.v1.model.Location location = new nl.markpost.demo.weather.api.v1.model.Location();
    location.setId(savedLocation.getLocationId());
    location.setName(savedLocation.getName());
    location.setLatitude(savedLocation.getLatitude());
    location.setLongitude(savedLocation.getLongitude());
    location.setCountry(savedLocation.getCountry());
    location.setCountryCode(savedLocation.getCountryCode());
    location.setAdmin1(savedLocation.getAdmin1());
    location.setTimezone(savedLocation.getTimezone());
    return location;
  }
}
