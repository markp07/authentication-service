package nl.markpost.demo.weather.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Daily {

  private LocalDateTime time;

  private LocalDateTime sunRise;

  private LocalDateTime sunSet;

  private WeatherCode weatherCode;

  private double temperatureMin;

  private double temperatureMax;

  private int precipitation;


}
