package nl.markpost.demo.weather.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.weather.api.v1.model.Location;
import nl.markpost.demo.weather.client.GeocodingClient;
import nl.markpost.demo.weather.entity.SavedLocation;
import nl.markpost.demo.weather.mapper.GeocodingMapper;
import nl.markpost.demo.weather.mapper.SavedLocationMapper;
import nl.markpost.demo.weather.model.GeocodingResponse;
import nl.markpost.demo.weather.repository.SavedLocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing saved locations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationsService {

  private final SavedLocationRepository savedLocationRepository;

  private final SavedLocationMapper savedLocationMapper;

  private final GeocodingClient geocodingClient;

  private final GeocodingMapper geocodingMapper;

  /**
   * Searches for locations by name.
   *
   * @param name the location name to search for
   * @return a list of matching locations
   */
  public List<Location> searchLocations(String name) {
    if (name == null || name.trim().isEmpty()) {
      return Collections.emptyList();
    }
    GeocodingResponse response = geocodingClient.searchLocations(name.trim(), 5, "en", "json");
    if (response == null || response.getResults() == null) {
      return Collections.emptyList();
    }
    return response.getResults().stream()
        .map(geocodingMapper::toLocationDto)
        .collect(Collectors.toList());
  }

  /**
   * Get all saved locations for a user.
   *
   * @param userId the user ID
   * @return list of saved locations
   */
  public List<Location> getSavedLocations(UUID userId) {
    log.info("Getting saved locations for user: {}", userId);
    return savedLocationRepository.findByUserIdOrderByDisplayOrderAsc(userId).stream()
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
    
    // Set display order to the end (max + 1)
    List<SavedLocation> existingLocations = savedLocationRepository.findByUserIdOrderByDisplayOrderAsc(userId);
    int maxOrder = existingLocations.stream()
        .map(SavedLocation::getDisplayOrder)
        .filter(order -> order != null)
        .max(Integer::compareTo)
        .orElse(-1);
    savedLocation.setDisplayOrder(maxOrder + 1);
    
    savedLocationRepository.save(savedLocation);
    return location;
  }

  /**
   * Delete a saved location.
   *
   * @param locationId the location ID (external geocoding ID)
   * @param userId     the user ID
   */
  @Transactional
  public void deleteSavedLocation(Long locationId, UUID userId) {
    log.info("Deleting saved location {} for user: {}", locationId, userId);
    savedLocationRepository.deleteByLocationIdAndUserId(locationId, userId);
  }

  /**
   * Reorder saved locations for a user.
   *
   * @param userId      the user ID
   * @param locationIds list of location IDs in the desired order
   */
  @Transactional
  public void reorderLocations(UUID userId, List<Long> locationIds) {
    log.info("Reordering locations for user: {}", userId);
    
    List<SavedLocation> savedLocations = savedLocationRepository.findByUserIdOrderByDisplayOrderAsc(userId);
    
    // Create a map of locationId to SavedLocation for quick lookup
    var locationMap = savedLocations.stream()
        .collect(Collectors.toMap(SavedLocation::getLocationId, loc -> loc));
    
    // Update display order based on the new order
    for (int i = 0; i < locationIds.size(); i++) {
      Long locationId = locationIds.get(i);
      SavedLocation location = locationMap.get(locationId);
      if (location != null && location.getUserId().equals(userId)) {
        location.setDisplayOrder(i);
        savedLocationRepository.save(location);
      }
    }
  }

}
