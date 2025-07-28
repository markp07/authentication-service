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

  private WeatherCode weatherCode;

  private Double temperatureMin;

  private Double temperatureMax;

  private Double precipitation;

  private Integer precipitationProbabilityMax;

  private LocalDateTime sunRise;

  private LocalDateTime sunSet;

}
