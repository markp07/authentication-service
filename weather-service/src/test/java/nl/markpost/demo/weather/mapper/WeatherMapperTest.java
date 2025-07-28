package nl.markpost.demo.weather.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;
import nl.markpost.demo.weather.model.CurrentResponse;
import nl.markpost.demo.weather.model.Daily;
import nl.markpost.demo.weather.model.DailyResponse;
import nl.markpost.demo.weather.model.Hourly;
import nl.markpost.demo.weather.model.HourlyResponse;
import nl.markpost.demo.weather.model.Weather;
import nl.markpost.demo.weather.model.WeatherCode;
import nl.markpost.demo.weather.model.WeatherResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class WeatherMapperTest {

  @Spy
  private final CurrentMapper currentMapper = new CurrentMapperImpl();

  @InjectMocks
  private final WeatherMapper mapper = new WeatherMapperImpl();

  @Test
  @DisplayName("Should map WeatherResponse to Weather with all fields filled")
  void toWeather_fullMapping() {
    DailyResponse dailyResponse = getDailyResponse();
    HourlyResponse hourlyResponse = getHourlyResponse();
    WeatherResponse response = getWeatherResponse(dailyResponse, hourlyResponse);

    Weather weather = mapper.toWeather(response);

    assertNotNull(weather);
    assertEquals(52.0, weather.getLatitude());
    assertEquals(4.0, weather.getLongitude());
    assertEquals("Europe/Berlin", weather.getTimezone());
    assertEquals(10.0, weather.getElevation());
    assertNotNull(weather.getCurrent());
    assertEquals(LocalDateTime.parse("2025-07-23T12:00:00"), weather.getCurrent().getTime());
    assertEquals(22.5, weather.getCurrent().getTemperature());
    assertEquals(5, weather.getCurrent().getWindSpeed());
    assertEquals(270, weather.getCurrent().getWindDirection());
    assertEquals(WeatherCode.MAINLY_CLEAR, weather.getCurrent().getWeatherCode());
    assertNotNull(weather.getDaily());
    assertEquals(2, weather.getDaily().size());
    Daily d1 = weather.getDaily().getFirst();
    assertEquals(LocalDateTime.parse("2025-07-23T00:00:00"), d1.getTime());
    assertEquals(LocalDateTime.parse("2025-07-23T05:13:00"), d1.getSunRise());
    assertEquals(LocalDateTime.parse("2025-07-23T21:12:00"), d1.getSunSet());
    assertEquals(WeatherCode.MAINLY_CLEAR, d1.getWeatherCode());
    assertEquals(15.0, d1.getTemperatureMin());
    assertEquals(25.5, d1.getTemperatureMax());
    assertEquals(0.0, d1.getPrecipitation());
    assertEquals(80, d1.getPrecipitationProbabilityMax());
    Daily d2 = weather.getDaily().get(1);
    assertEquals(LocalDateTime.parse("2025-07-24T00:00:00"), d2.getTime());
    assertEquals(LocalDateTime.parse("2025-07-24T05:14:00"), d2.getSunRise());
    assertEquals(LocalDateTime.parse("2025-07-24T21:11:00"), d2.getSunSet());
    assertEquals(WeatherCode.RAIN_SHOWERS_SLIGHT, d2.getWeatherCode());
    assertEquals(16.5, d2.getTemperatureMin());
    assertEquals(27.0, d2.getTemperatureMax());
    assertEquals(1.2, d2.getPrecipitation());
    assertEquals(90, d2.getPrecipitationProbabilityMax());
    // Hourly assertions
    assertNotNull(weather.getHourly());
    assertEquals(2, weather.getHourly().size());
    Hourly h1 = weather.getHourly().getFirst();
    assertEquals(LocalDateTime.parse("2025-07-23T13:00:00"), h1.getTime());
    assertEquals(WeatherCode.MAINLY_CLEAR, h1.getWeatherCode());
    assertEquals(21.0, h1.getTemperature());
    assertEquals(10, h1.getPrecipitationProbability());
    Hourly h2 = weather.getHourly().get(1);
    assertEquals(LocalDateTime.parse("2025-07-23T14:00:00"), h2.getTime());
    assertEquals(WeatherCode.RAIN_SHOWERS_SLIGHT, h2.getWeatherCode());
    assertEquals(22.0, h2.getTemperature());
    assertEquals(20, h2.getPrecipitationProbability());
  }

  private static DailyResponse getDailyResponse() {
    DailyResponse dailyResponse = new DailyResponse();
    dailyResponse.setTime(List.of("2025-07-23", "2025-07-24"));
    dailyResponse.setWeather_code(List.of(1, 80));
    dailyResponse.setTemperature_2m_max(List.of(25.5, 27.0));
    dailyResponse.setTemperature_2m_min(List.of(15.0, 16.5));
    dailyResponse.setSunrise(List.of("2025-07-23T05:13:00", "2025-07-24T05:14:00"));
    dailyResponse.setSunset(List.of("2025-07-23T21:12:00", "2025-07-24T21:11:00"));
    dailyResponse.setPrecipitation_sum(List.of(0.0, 1.2));
    dailyResponse.setPrecipitation_probability_max(List.of(80, 90));
    return dailyResponse;
  }

  private static HourlyResponse getHourlyResponse() {
    HourlyResponse hourlyResponse = new HourlyResponse();
    hourlyResponse.setTime(List.of("2025-07-23T13:00:00", "2025-07-23T14:00:00"));
    hourlyResponse.setWeather_code(List.of(1, 80));
    hourlyResponse.setTemperature_2m(List.of(21.0, 22.0));
    hourlyResponse.setPrecipitation_probability(List.of(10, 20));
    return hourlyResponse;
  }

  private static WeatherResponse getWeatherResponse(DailyResponse dailyResponse,
      HourlyResponse hourlyResponse) {
    CurrentResponse currentResponse = new CurrentResponse();
    currentResponse.setTime("2025-07-23T12:00:00");
    currentResponse.setWeather_code(1);
    currentResponse.setTemperature_2m(22.5);
    currentResponse.setWind_speed_10m(5);
    currentResponse.setWind_direction_10m(270);

    WeatherResponse response = new WeatherResponse();
    response.setLatitude(52.0);
    response.setLongitude(4.0);
    response.setTimezone("Europe/Berlin");
    response.setElevation(10.0);
    response.setCurrent(currentResponse);
    response.setDaily(dailyResponse);
    response.setHourly(hourlyResponse);
    return response;
  }
}