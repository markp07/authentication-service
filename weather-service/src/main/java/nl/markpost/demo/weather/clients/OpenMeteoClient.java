package nl.markpost.demo.weather.clients;

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
     * @param latitude the latitude
     * @param longitude the longitude
     * @return a Mono emitting the raw JSON response
     */
    @GetMapping("/v1/forecast?current=temperature_2m,relative_humidity_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m")
    WeatherResponse getWeather(@RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude);
}
//TODO: https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_sum&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,precipitation&timezone=Europe%2FBerlin&forecast_days=14
//Update models