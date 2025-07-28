package nl.markpost.demo.weather.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

}

