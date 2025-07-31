package nl.markpost.demo.weather.service;

import lombok.RequiredArgsConstructor;
import nl.markpost.demo.weather.client.OpenMeteoClient;
import nl.markpost.demo.weather.mapper.WeatherMapper;
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

  private final WeatherMapper weatherMapper;

  /**
   * Retrieves daily weather data for the given coordinates, cached for 24h or until midnight.
   */
  @Cacheable(value = "weatherDaily", key = "#latitude + '-' + #longitude")
  public WeatherResponse getWeatherDaily(double latitude, double longitude) {
    return openMeteoClient.getWeatherDaily(latitude, longitude);
  }

  /**
   * Retrieves hourly weather data for the given coordinates, cached for 1 hour.
   */
  @Cacheable(value = "weatherHourly", key = "#latitude + '-' + #longitude")
  public WeatherResponse getWeatherHourly(double latitude, double longitude) {
    return openMeteoClient.getWeatherHourly(latitude, longitude);
  }

  /**
   * Retrieves and maps weather data for the given coordinates.
   */
  public Weather getWeather(double latitude, double longitude) {
    WeatherResponse dailyWeatherResponse = getWeatherDaily(latitude, longitude);
    WeatherResponse hourlyWeatherResponse = getWeatherHourly(latitude, longitude);
    hourlyWeatherResponse.setDaily(dailyWeatherResponse.getDaily());
    return weatherMapper.toWeather(hourlyWeatherResponse);
  }
}
