package nl.markpost.demo.weather.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing the hourly weather data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyResponse {

  private List<String> time;

  private List<Integer> weather_code;

  private List<Double> temperature_2m;

  private List<Integer> precipitation_probability;

}

