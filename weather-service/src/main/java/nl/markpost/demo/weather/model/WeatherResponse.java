package nl.markpost.demo.weather.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing the weather response returned by the API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {

  private double latitude;

  private double longitude;

  private double generationtime_ms;

  private int utc_offset_seconds;

  private String timezone;

  private String timezone_abbreviation;

  private double elevation;

  private Map<String, String> current_units;

  private CurrentResponse current;

  private Map<String, String> hourly_units;

  private HourlyResponse hourly;

  private Map<String, String> daily_units;

  private DailyResponse daily;
}
