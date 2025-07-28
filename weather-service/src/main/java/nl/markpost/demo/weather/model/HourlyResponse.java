package nl.markpost.demo.weather.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

