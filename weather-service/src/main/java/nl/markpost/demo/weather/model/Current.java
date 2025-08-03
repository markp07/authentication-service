package nl.markpost.demo.weather.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing the current weather data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Current {

    private LocalDateTime time;

    private WeatherCode weatherCode;

    private Double temperature;

    private Integer windSpeed;

    private WindDirection windDirection;

}
