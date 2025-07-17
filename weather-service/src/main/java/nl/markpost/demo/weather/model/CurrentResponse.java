package nl.markpost.demo.weather.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing the current weather data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentResponse {
    private String time;
    private int interval;
    private double temperature_2m;
    Integer relative_humidity_2m;
    private double wind_speed_10m;
}

