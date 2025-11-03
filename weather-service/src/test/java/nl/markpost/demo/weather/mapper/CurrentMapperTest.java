package nl.markpost.demo.weather.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import nl.markpost.demo.weather.model.Current;
import nl.markpost.demo.weather.model.CurrentResponse;
import nl.markpost.demo.weather.model.WeatherCode;
import nl.markpost.demo.weather.model.WindDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CurrentMapperTest {

  private final CurrentMapper mapper = new CurrentMapperImpl();

  @Test
  @DisplayName("Should map CurrentResponse to Current with all fields filled")
  void toCurrent_fullMapping() {
    CurrentResponse response = new CurrentResponse();
    response.setTime("2025-07-23T12:00:00");
    response.setWeather_code(1);
    response.setTemperature_2m(22.5);
    response.setWind_speed_10m(5);
    response.setWind_direction_10m(270);

    Current current = mapper.toCurrent(response);

    assertNotNull(current);
    assertEquals(LocalDateTime.parse("2025-07-23T12:00:00"), current.getTime());
    assertEquals(WeatherCode.MAINLY_CLEAR, current.getWeatherCode());
    assertEquals(22.5, current.getTemperature());
    assertEquals(5, current.getWindSpeed());
    assertEquals(WindDirection.W, current.getWindDirection());
  }

  @Test
  @DisplayName("Should map wind direction degrees to correct WindDirection enum")
  void toCurrent_windDirectionMapping() {
    CurrentResponse response = new CurrentResponse();
    response.setTime("2025-07-23T12:00:00");
    response.setWeather_code(1);
    response.setTemperature_2m(22.5);
    response.setWind_speed_10m(5);

    response.setWind_direction_10m(45); // NE
    Current current = mapper.toCurrent(response);
    assertEquals(WindDirection.NE, current.getWindDirection());

    response.setWind_direction_10m(90); // E
    current = mapper.toCurrent(response);
    assertEquals(WindDirection.E, current.getWindDirection());

    response.setWind_direction_10m(205); // SW
    current = mapper.toCurrent(response);
    assertEquals(WindDirection.SW, current.getWindDirection());

    response.setWind_direction_10m(310); // NW
    current = mapper.toCurrent(response);
    assertEquals(WindDirection.NW, current.getWindDirection());
  }
}