package nl.markpost.demo.weather.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Weather {

  private double latitude;

  private double longitude;

  private String timezone;

  private double elevation;

  private Current current;

  private List<Hourly> hourly;

}

