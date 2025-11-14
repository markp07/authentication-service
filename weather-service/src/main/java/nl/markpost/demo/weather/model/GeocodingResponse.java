package nl.markpost.demo.weather.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from the Open-Meteo Geocoding API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeocodingResponse {

  private List<GeocodingResult> results;
}
