package nl.markpost.demo.weather.client;

import nl.markpost.demo.weather.model.GeocodingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Client for calling the Open-Meteo Geocoding API.
 */
@FeignClient(name = "geocodingClient", url = "https://geocoding-api.open-meteo.com")
public interface GeocodingClient {

  /**
   * Searches for locations by name.
   *
   * @param name the location name to search for
   * @param count the maximum number of results to return
   * @param language the language for the results
   * @return the geocoding response containing matching locations
   */
  @GetMapping("/v1/search")
  GeocodingResponse searchLocations(
      @RequestParam("name") String name,
      @RequestParam("count") int count,
      @RequestParam("language") String language,
      @RequestParam("format") String format
  );
}
