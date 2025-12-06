package nl.markpost.demo.weather.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import nl.markpost.demo.weather.api.v1.model.CurrentResponse;
import nl.markpost.demo.weather.api.v1.model.DailyResponse;
import nl.markpost.demo.weather.api.v1.model.HourlyResponse;
import nl.markpost.demo.weather.api.v1.model.WeatherCodeResponse;
import nl.markpost.demo.weather.api.v1.model.WeatherResponse;
import nl.markpost.demo.weather.api.v1.model.WindDirectionResponse;
import nl.markpost.demo.weather.model.Current;
import nl.markpost.demo.weather.model.Daily;
import nl.markpost.demo.weather.model.Hourly;
import nl.markpost.demo.weather.model.Weather;
import nl.markpost.demo.weather.model.WeatherCode;
import nl.markpost.demo.weather.model.WindDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class WeatherModelMapperTest {

  private final WeatherModelMapper mapper = Mappers.getMapper(WeatherModelMapper.class);

  @Test
  @DisplayName("Should map Weather domain to WeatherResponse API model")
  void fromWeather_success() {
    Current current = new Current(
        LocalDateTime.of(2025, Month.APRIL, 1, 12, 0),
        WeatherCode.CLEAR_SKY,
        15.5,
        10,
        WindDirection.NW
    );
    Weather weather = new Weather(
        52.37,
        4.89,
        "Amsterdam",
        "Europe/Amsterdam",
        5.0,
        current,
        List.of(),
        List.of()
    );

    WeatherResponse response = mapper.from(weather);

    assertNotNull(response);
    assertEquals(52.37, response.getLatitude());
    assertEquals(4.89, response.getLongitude());
    assertEquals("Amsterdam", response.getLocation());
    assertEquals("Europe/Amsterdam", response.getTimezone());
    assertEquals(5.0, response.getElevation());
    assertNotNull(response.getCurrent());
  }

  @Test
  @DisplayName("Should map Current domain to CurrentResponse API model")
  void fromCurrent_success() {
    Current current = new Current(
        LocalDateTime.of(2025, Month.APRIL, 1, 12, 0),
        WeatherCode.PARTLY_CLOUDY,
        20.0,
        15,
        WindDirection.E
    );

    CurrentResponse response = mapper.from(current);

    assertNotNull(response);
    assertEquals("2025-04-01T12:00:00", response.getTime());
    assertEquals(WeatherCodeResponse.PARTLY_CLOUDY, response.getWeatherCode());
    assertEquals(20.0, response.getTemperature());
    assertEquals(15, response.getWindSpeed());
    assertEquals(WindDirectionResponse.E, response.getWindDirection());
  }

  @Test
  @DisplayName("Should map Daily domain to DailyResponse API model")
  void fromDaily_success() {
    Daily daily = new Daily(
        LocalDateTime.of(2025, Month.APRIL, 1, 0, 0),
        WeatherCode.RAIN_SLIGHT,
        10.0,
        18.0,
        5.5,
        60,
        20,
        WindDirection.SW,
        LocalDateTime.of(2025, Month.APRIL, 1, 6, 30),
        LocalDateTime.of(2025, Month.APRIL, 1, 20, 15)
    );

    DailyResponse response = mapper.from(daily);

    assertNotNull(response);
    assertEquals("2025-04-01T00:00:00", response.getTime());
    assertEquals(WeatherCodeResponse.RAIN_SLIGHT, response.getWeatherCode());
    assertEquals(10.0, response.getTemperatureMin());
    assertEquals(18.0, response.getTemperatureMax());
    assertEquals(5.5, response.getPrecipitation());
    assertEquals(60, response.getPrecipitationProbabilityMax());
    assertEquals(20, response.getWindSpeed());
    assertEquals(WindDirectionResponse.SW, response.getWindDirection());
    assertEquals("2025-04-01T06:30:00", response.getSunRise());
    assertEquals("2025-04-01T20:15:00", response.getSunSet());
  }

  @Test
  @DisplayName("Should map Hourly domain to HourlyResponse API model")
  void fromHourly_success() {
    Hourly hourly = new Hourly(
        LocalDateTime.of(2025, Month.APRIL, 1, 14, 0),
        WeatherCode.OVERCAST,
        17.5,
        0.0,
        10,
        12,
        WindDirection.N
    );

    HourlyResponse response = mapper.from(hourly);

    assertNotNull(response);
    assertEquals("2025-04-01T14:00:00", response.getTime());
    assertEquals(WeatherCodeResponse.OVERCAST, response.getWeatherCode());
    assertEquals(17.5, response.getTemperature());
    assertEquals(0.0, response.getPrecipitation());
    assertEquals(10, response.getPrecipitationProbability());
    assertEquals(12, response.getWindSpeed());
    assertEquals(WindDirectionResponse.N, response.getWindDirection());
  }

  @Test
  @DisplayName("Should handle null Weather")
  void fromWeather_nullInput() {
    WeatherResponse response = mapper.from((Weather) null);
    assertNull(response);
  }

  @Test
  @DisplayName("Should handle null Current")
  void fromCurrent_nullInput() {
    CurrentResponse response = mapper.from((Current) null);
    assertNull(response);
  }

  @Test
  @DisplayName("Should handle null Daily")
  void fromDaily_nullInput() {
    DailyResponse response = mapper.from((Daily) null);
    assertNull(response);
  }

  @Test
  @DisplayName("Should handle null Hourly")
  void fromHourly_nullInput() {
    HourlyResponse response = mapper.from((Hourly) null);
    assertNull(response);
  }

  @Test
  @DisplayName("Should map null LocalDateTime to null string")
  void mapLocalDateTime_null() {
    String result = mapper.map((LocalDateTime) null);
    assertNull(result);
  }

  @Test
  @DisplayName("Should map LocalDateTime to ISO string")
  void mapLocalDateTime_success() {
    LocalDateTime dateTime = LocalDateTime.of(2025, Month.APRIL, 1, 12, 30, 45);
    String result = mapper.map(dateTime);
    assertEquals("2025-04-01T12:30:45", result);
  }

  @Test
  @DisplayName("Should map null WeatherCode to null")
  void mapWeatherCode_null() {
    WeatherCodeResponse result = mapper.map((WeatherCode) null);
    assertNull(result);
  }

  @Test
  @DisplayName("Should map WeatherCode enum correctly")
  void mapWeatherCode_success() {
    WeatherCodeResponse result = mapper.map(WeatherCode.RAIN_SLIGHT);
    assertEquals(WeatherCodeResponse.RAIN_SLIGHT, result);
  }

  @Test
  @DisplayName("Should map null WindDirection to null")
  void mapWindDirection_null() {
    WindDirectionResponse result = mapper.map((WindDirection) null);
    assertNull(result);
  }

  @Test
  @DisplayName("Should map WindDirection enum correctly")
  void mapWindDirection_success() {
    WindDirectionResponse result = mapper.map(WindDirection.SE);
    assertEquals(WindDirectionResponse.SE, result);
  }
}
