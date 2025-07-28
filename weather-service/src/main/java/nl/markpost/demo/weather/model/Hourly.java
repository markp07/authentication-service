package nl.markpost.demo.weather.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hourly {

  private LocalDateTime time;

  private WeatherCode weatherCode;

  private Double temperature;

  private Integer precipitationProbability;


}
