package nl.markpost.demo.weather.client;

import nl.markpost.demo.weather.model.WeatherResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client for calling the Open-Meteo weather API.
 */
@FeignClient(name = "openMeteoClient", url = "https://api.open-meteo.com")
public interface OpenMeteoClient {

  /**
   * Calls the Open-Meteo API for the given coordinates.
   *
   * @param latitude  the latitude
   * @param longitude the longitude
   * @return a Mono emitting the raw JSON response
   */
  @GetMapping("/v1/forecast?daily=weather_code,temperature_2m_max,precipitation_probability_max,temperature_2m_min,precipitation_sum,sunrise,sunset&hourly=temperature_2m,weather_code,precipitation_probability&current=temperature_2m,weather_code,wind_speed_10m,wind_direction_10m&timezone=Europe%2FBerlin&forecast_days=14")
  WeatherResponse getWeather(@RequestParam("latitude") double latitude,
      @RequestParam("longitude") double longitude);

}