package nl.markpost.demo.weather.client;

import nl.markpost.demo.weather.model.ReverseGeocodeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "reverseGeocodeClient", url = "https://api-bdc.io")
public interface ReverseGeocodeClient {

  @GetMapping("/data/reverse-geocode-client?localityLanguage=en")
  ReverseGeocodeResponse getLocation(@RequestParam("latitude") double latitude,
      @RequestParam("longitude") double longitude);
}

