package nl.markpost.demo.weather.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing the daily weather data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyResponse {

  private List<String> time;

  private List<Integer> weather_code;

  private List<Double> temperature_2m_max;

  private List<Double> temperature_2m_min;

  private List<String> sunrise;

  private List<String> sunset;

  private List<Double> precipitation_sum;

  private List<Integer> precipitation_probability_max;

  private List<Integer> wind_speed_10m_max;

  private List<Integer> wind_direction_10m_dominant;

}

