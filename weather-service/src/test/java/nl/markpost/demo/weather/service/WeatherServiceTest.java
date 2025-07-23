package nl.markpost.demo.weather.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nl.markpost.demo.weather.client.OpenMeteoClient;
import nl.markpost.demo.weather.mapper.WeatherMapper;
import nl.markpost.demo.weather.model.Weather;
import nl.markpost.demo.weather.model.WeatherResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

  @Mock
  private OpenMeteoClient openMeteoClient;

  @Mock
  private WeatherMapper weatherMapper;

  @InjectMocks
  private WeatherService weatherService;

  @Test
  @DisplayName("Should call OpenMeteoClient and WeatherMapper and return mapped Weather")
  void getWeather_success() {
    double latitude = 52.0;
    double longitude = 4.0;
    WeatherResponse weatherResponse = mock(WeatherResponse.class);
    Weather expectedWeather = mock(Weather.class);

    when(openMeteoClient.getWeather(latitude, longitude)).thenReturn(weatherResponse);
    when(weatherMapper.toWeather(weatherResponse)).thenReturn(expectedWeather);

    Weather result = weatherService.getWeather(latitude, longitude);

    assertSame(expectedWeather, result);
    verify(openMeteoClient).getWeather(latitude, longitude);
    verify(weatherMapper).toWeather(weatherResponse);
  }

  @Test
  @DisplayName("Should handle null WeatherResponse from client")
  void getWeather_nullResponse() {
    double latitude = 52.0;
    double longitude = 4.0;
    when(openMeteoClient.getWeather(latitude, longitude)).thenReturn(null);
    when(weatherMapper.toWeather(null)).thenReturn(null);
    Weather result = weatherService.getWeather(latitude, longitude);
    assertNull(result);
    verify(openMeteoClient).getWeather(latitude, longitude);
    verify(weatherMapper).toWeather(null);
  }
}