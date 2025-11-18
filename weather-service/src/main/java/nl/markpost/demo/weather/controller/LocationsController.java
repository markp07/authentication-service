package nl.markpost.demo.weather.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.weather.api.v1.controller.LocationsApi;
import nl.markpost.demo.weather.api.v1.model.Location;
import nl.markpost.demo.weather.service.LocationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for locations retrieval API.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class LocationsController implements LocationsApi {

  private final LocationsService locationsService;
  private final HttpServletRequest request;

  /**
   * Searches for locations by name.
   *
   * @param name the location name to search for
   * @return ResponseEntity with list of matching locations
   */
  @Override
  public ResponseEntity<List<Location>> searchLocations(String name) {
    log.info("Searching for locations with name: {}", name);
    List<Location> locations = locationsService.searchLocations(name);
    return ResponseEntity.ok(locations);
  }

  /**
   * Gets all saved locations for the authenticated user.
   *
   * @return ResponseEntity with list of saved locations
   */
  @Override
  public ResponseEntity<List<Location>> getSavedLocations() {
    UUID userId = getUserIdFromToken();
    log.info("Getting saved locations for user: {}", userId);
    List<Location> locations = locationsService.getSavedLocations(userId);
    return ResponseEntity.ok(locations);
  }

  /**
   * Saves a location for the authenticated user.
   *
   * @param location the location to save
   * @return ResponseEntity with the saved location
   */
  @Override
  public ResponseEntity<Location> saveLocation(Location location) {
    UUID userId = getUserIdFromToken();
    log.info("Saving location {} for user: {}", location.getName(), userId);
    Location savedLocation = locationsService.saveLocation(userId, location);
    return ResponseEntity.ok(savedLocation);
  }

  /**
   * Deletes a saved location.
   *
   * @param id the saved location ID
   * @return ResponseEntity with no content
   */
  @Override
  public ResponseEntity<Void> deleteSavedLocation(Long id) {
    UUID userId = getUserIdFromToken();
    log.info("Deleting saved location {} for user: {}", id, userId);
    locationsService.deleteSavedLocation(id, userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * Extracts the user ID from the JWT token.
   *
   * @return the user ID
   */
  private UUID getUserIdFromToken() {
    Claims claims = (Claims) request.getAttribute("jwtClaims");
    String userIdStr = claims.get("userId", String.class);
    return UUID.fromString(userIdStr);
  }
}
