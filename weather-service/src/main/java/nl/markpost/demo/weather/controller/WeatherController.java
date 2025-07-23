package nl.markpost.demo.weather.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.weather.model.Weather;
import nl.markpost.demo.weather.model.WeatherResponse;
import nl.markpost.demo.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for weather retrieval API.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private final WeatherService weatherService;

    /**
     * Retrieves weather data for the given coordinates.
     * @param latitude the latitude
     * @param longitude the longitude
     * @return a Mono emitting the mapped WeatherResponse
     */
    @GetMapping
    public ResponseEntity<Weather> getWeather(@RequestParam double latitude, @RequestParam double longitude) {
        log.info("Receive weather data at latitude: {}, longitude: {}", latitude, longitude);
        return ResponseEntity.ok(weatherService.getWeather(latitude, longitude));

    }
}
