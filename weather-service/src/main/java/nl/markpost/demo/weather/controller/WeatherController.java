package nl.markpost.demo.weather.controller;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.weather.api.v1.controller.WeatherApi;
import nl.markpost.demo.weather.mapper.WeatherModelMapper;
import nl.markpost.demo.weather.service.SavedLocationService;
import nl.markpost.demo.weather.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for weather retrieval API.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class WeatherController implements WeatherApi {

  private final WeatherService weatherService;
  private final WeatherModelMapper weatherModelMapper;
  private final SavedLocationService savedLocationService;
  private final HttpServletRequest request;

  /**
   * Retrieves weather data for the given coordinates.
   *
   * @param latitude  the latitude
   * @param longitude the longitude
   * @return ResponseEntity with weather forecast
   */
  @Override
  public ResponseEntity<nl.markpost.demo.weather.api.v1.model.Weather> getWeather(Double latitude,
      Double longitude) {
    log.info("Receive weather data at latitude: {}, longitude: {}", latitude, longitude);
    nl.markpost.demo.weather.model.Weather weather = weatherService.getWeather(latitude, longitude);
    return ResponseEntity.ok(weatherModelMapper.toApiModel(weather));
  }

  /**
   * Searches for locations by name.
   *
   * @param name the location name to search for
   * @return ResponseEntity with list of matching locations
   */
  @Override
  public ResponseEntity<java.util.List<nl.markpost.demo.weather.api.v1.model.Location>> searchLocations(
      String name) {
    log.info("Searching for locations with name: {}", name);
    java.util.List<nl.markpost.demo.weather.api.v1.model.Location> locations = weatherService.searchLocations(
        name);
    return ResponseEntity.ok(locations);
  }

  /**
   * Gets all saved locations for the authenticated user.
   *
   * @return ResponseEntity with list of saved locations
   */
  @Override
  public ResponseEntity<java.util.List<nl.markpost.demo.weather.api.v1.model.Location>> getSavedLocations() {
    UUID userId = getUserIdFromToken();
    log.info("Getting saved locations for user: {}", userId);
    java.util.List<nl.markpost.demo.weather.api.v1.model.Location> locations = savedLocationService.getSavedLocations(
        userId);
    return ResponseEntity.ok(locations);
  }

  /**
   * Saves a location for the authenticated user.
   *
   * @param location the location to save
   * @return ResponseEntity with the saved location
   */
  @Override
  public ResponseEntity<nl.markpost.demo.weather.api.v1.model.Location> saveLocation(
      nl.markpost.demo.weather.api.v1.model.Location location) {
    UUID userId = getUserIdFromToken();
    log.info("Saving location {} for user: {}", location.getName(), userId);
    nl.markpost.demo.weather.api.v1.model.Location savedLocation = savedLocationService.saveLocation(
        userId, location);
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
    savedLocationService.deleteSavedLocation(id, userId);
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
