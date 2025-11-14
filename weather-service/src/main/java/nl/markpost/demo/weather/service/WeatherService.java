package nl.markpost.demo.weather.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import nl.markpost.demo.weather.client.GeocodingClient;
import nl.markpost.demo.weather.client.OpenMeteoClient;
import nl.markpost.demo.weather.client.ReverseGeocodeClient;
import nl.markpost.demo.weather.mapper.GeocodingMapper;
import nl.markpost.demo.weather.mapper.WeatherMapper;
import nl.markpost.demo.weather.model.GeocodingResponse;
import nl.markpost.demo.weather.model.ReverseGeocodeResponse;
import nl.markpost.demo.weather.model.Weather;
import nl.markpost.demo.weather.model.WeatherResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving and mapping weather data from the Open-Meteo API.
 */
@Service
@RequiredArgsConstructor
public class WeatherService {

  private final OpenMeteoClient openMeteoClient;

  private final ReverseGeocodeClient reverseGeocodeClient;

  private final GeocodingClient geocodingClient;

  private final WeatherMapper weatherMapper;

  private final GeocodingMapper geocodingMapper;

  /**
   * Retrieves and maps weather data for the given coordinates.
   */
  public Weather getWeather(double latitude, double longitude) {
    WeatherResponse dailyWeatherResponse = getWeatherDaily(latitude, longitude);
    WeatherResponse hourlyWeatherResponse = getWeatherHourly(latitude, longitude);
    ReverseGeocodeResponse reverseGeocodeResponse = getLocation(latitude, longitude);
    if (dailyWeatherResponse != null && hourlyWeatherResponse != null) {
      hourlyWeatherResponse.setDaily(dailyWeatherResponse.getDaily());
    }
    return weatherMapper.toWeather(hourlyWeatherResponse, reverseGeocodeResponse);
  }

  /**
   * Retrieves daily weather data for the given coordinates, cached for 24h or until midnight.
   */
  @Cacheable(value = "weatherDaily", key = "#latitude + '-' + #longitude")
  private WeatherResponse getWeatherDaily(double latitude, double longitude) {
    return openMeteoClient.getWeatherDaily(latitude, longitude);
  }

  /**
   * Retrieves hourly weather data for the given coordinates, cached for 1 hour.
   */
  @Cacheable(value = "weatherHourly", key = "#latitude + '-' + #longitude")
  private WeatherResponse getWeatherHourly(double latitude, double longitude) {
    return openMeteoClient.getWeatherHourly(latitude, longitude);
  }

  /**
   * Retrieves the location name for the given coordinates.
   */
  @Cacheable(value = "location", key = "#latitude + '-' + #longitude")
  private ReverseGeocodeResponse getLocation(double latitude, double longitude) {
    return reverseGeocodeClient.getLocation(latitude, longitude);
  }

  /**
   * Searches for locations by name.
   *
   * @param name the location name to search for
   * @return a list of matching locations
   */
  public List<nl.markpost.demo.weather.api.v1.model.Location> searchLocations(String name) {
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
}
