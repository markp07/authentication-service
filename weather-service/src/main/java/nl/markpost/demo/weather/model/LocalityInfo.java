package nl.markpost.demo.weather.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalityInfo {

  private List<Administrative> administrative;
  private List<Informative> informative;
}

