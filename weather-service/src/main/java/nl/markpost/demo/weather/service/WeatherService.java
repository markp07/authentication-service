package nl.markpost.demo.weather.service;

import lombok.RequiredArgsConstructor;
import nl.markpost.demo.weather.client.OpenMeteoClient;
import nl.markpost.demo.weather.mapper.WeatherMapper;
import nl.markpost.demo.weather.model.Weather;
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
   * Retrieves weather data for the given coordinates.
   *
   * @param latitude  the latitude
   * @param longitude the longitude
   * @return a Mono emitting the mapped WeatherResponse
   */
  public Weather getWeather(double latitude, double longitude) {
    return weatherMapper.toWeather(openMeteoClient.getWeather(latitude, longitude));
  }
}
