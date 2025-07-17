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
    private List<Double> temperature_2m;
    private List<Integer> relative_humidity_2m;
    private List<Double> wind_speed_10m;
}

