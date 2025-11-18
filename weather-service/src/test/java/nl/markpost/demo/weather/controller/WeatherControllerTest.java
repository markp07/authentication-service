package nl.markpost.demo.weather.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import nl.markpost.demo.weather.api.v1.model.WeatherResponse;
import nl.markpost.demo.weather.config.SecurityConfig;
import nl.markpost.demo.weather.filter.JwtAuthenticationFilter;
import nl.markpost.demo.weather.mapper.WeatherModelMapper;
import nl.markpost.demo.weather.model.Current;
import nl.markpost.demo.weather.model.Weather;
import nl.markpost.demo.weather.model.WeatherCode;
import nl.markpost.demo.weather.model.WindDirection;
import nl.markpost.demo.weather.service.WeatherService;
import nl.markpost.demo.weather.util.ObjectMapperUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = WeatherController.class)
@ActiveProfiles(value = "ut")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class WeatherControllerTest {

  @MockitoBean
  private WeatherService weatherService;

  @MockitoBean
  private WeatherModelMapper weatherModelMapper;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("Should return weather data for valid coordinates")
  void getWeather_success() throws Exception {
    Current current = new Current(
        LocalDateTime.of(2025, Month.APRIL, 1, 12, 0),
        WeatherCode.FOG,
        1.0,
        6,
        WindDirection.S
    );
    Weather weather = new Weather(
        52.0,
        4.0,
        "Gouda",
        "Europe/Berlin",
        10.0,
        current,
        List.of(),
        List.of()
    );
    WeatherResponse apiWeather = new WeatherResponse();
    apiWeather.setLatitude(52.0);
    apiWeather.setLongitude(4.0);
    apiWeather.setLocation("Gouda");
    apiWeather.setTimezone("Europe/Berlin");
    apiWeather.setElevation(10.0);

    when(weatherService.getWeather(52.0, 4.0)).thenReturn(weather);
    when(weatherModelMapper.from(weather)).thenReturn(apiWeather);

    MvcResult result = mockMvc.perform(
            get("/v1/forecast")
                .param("latitude", "52.0")
                .param("longitude", "4.0")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();
    String responseContent = result.getResponse().getContentAsString();

    assertNotNull(responseContent);
    WeatherResponse response = ObjectMapperUtil.createObjectMapper()
        .readValue(responseContent, WeatherResponse.class);
    assertEquals(apiWeather.getLatitude(), response.getLatitude());
    assertEquals(apiWeather.getLocation(), response.getLocation());
  }

  @Test
  @DisplayName("Should return 400 Bad Request if latitude is missing")
  void getWeather_missingLatitude() throws Exception {
    mockMvc.perform(get("/v1/forecast")
            .param("longitude", "4.0"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should return 400 Bad Request if longitude is missing")
  void getWeather_missingLongitude() throws Exception {
    mockMvc.perform(get("/v1/forecast")
            .param("latitude", "52.0"))
        .andExpect(status().isBadRequest());
  }

}