package nl.markpost.demo.weather.service;

import lombok.RequiredArgsConstructor;
import nl.markpost.demo.weather.client.OpenMeteoClient;
import nl.markpost.demo.weather.client.ReverseGeocodeClient;
import nl.markpost.demo.weather.mapper.WeatherMapper;
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

  private final WeatherMapper weatherMapper;

  /**
   * Retrieves and maps weather data for the given coordinates.
   */
  public Weather getWeather(double latitude, double longitude) {
    WeatherResponse dailyWeatherResponse = getWeatherDaily(latitude, longitude);
    WeatherResponse hourlyWeatherResponse = getWeatherHourly(latitude, longitude);
    ReverseGeocodeResponse reverseGeocodeResponse = getLocation(latitude, longitude);
    hourlyWeatherResponse.setDaily(dailyWeatherResponse.getDaily());
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
}
