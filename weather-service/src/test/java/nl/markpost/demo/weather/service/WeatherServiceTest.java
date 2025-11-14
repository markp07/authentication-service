package nl.markpost.demo.weather.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import nl.markpost.demo.weather.client.GeocodingClient;
import nl.markpost.demo.weather.client.OpenMeteoClient;
import nl.markpost.demo.weather.client.ReverseGeocodeClient;
import nl.markpost.demo.weather.mapper.GeocodingMapper;
import nl.markpost.demo.weather.mapper.WeatherMapper;
import nl.markpost.demo.weather.model.GeocodingResponse;
import nl.markpost.demo.weather.model.GeocodingResult;
import nl.markpost.demo.weather.model.ReverseGeocodeResponse;
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

  @Mock
  private ReverseGeocodeClient reverseGeocodeClient;

  @Mock
  private GeocodingClient geocodingClient;

  @Mock
  private GeocodingMapper geocodingMapper;

  @InjectMocks
  private WeatherService weatherService;

  @Test
  @DisplayName("Should call OpenMeteoClient, ReverseGeocodeClient, and WeatherMapper and return mapped Weather")
  void getWeather_success() {
    double latitude = 52.0;
    double longitude = 4.0;
    WeatherResponse weatherResponse = mock(WeatherResponse.class);
    ReverseGeocodeResponse reverseGeocodeResponse = mock(ReverseGeocodeResponse.class);
    Weather expectedWeather = mock(Weather.class);

    when(openMeteoClient.getWeatherDaily(latitude, longitude)).thenReturn(weatherResponse);
    when(openMeteoClient.getWeatherHourly(latitude, longitude)).thenReturn(weatherResponse);
    when(reverseGeocodeClient.getLocation(latitude, longitude)).thenReturn(reverseGeocodeResponse);
    when(weatherMapper.toWeather(weatherResponse, reverseGeocodeResponse)).thenReturn(
        expectedWeather);

    Weather result = weatherService.getWeather(latitude, longitude);

    assertSame(expectedWeather, result);
    verify(openMeteoClient).getWeatherDaily(latitude, longitude);
    verify(openMeteoClient).getWeatherHourly(latitude, longitude);
    verify(reverseGeocodeClient).getLocation(latitude, longitude);
    verify(weatherMapper).toWeather(weatherResponse, reverseGeocodeResponse);
  }

  @Test
  @DisplayName("Should handle null WeatherResponse from client")
  void getWeather_nullResponse() {
    double latitude = 52.0;
    double longitude = 4.0;
    when(openMeteoClient.getWeatherDaily(latitude, longitude)).thenReturn(null);
    when(openMeteoClient.getWeatherHourly(latitude, longitude)).thenReturn(null);
    when(reverseGeocodeClient.getLocation(latitude, longitude)).thenReturn(null);
    when(weatherMapper.toWeather(null, null)).thenReturn(null);
    Weather result = weatherService.getWeather(latitude, longitude);
    assertNull(result);
    verify(openMeteoClient).getWeatherDaily(latitude, longitude);
    verify(openMeteoClient).getWeatherHourly(latitude, longitude);
    verify(reverseGeocodeClient).getLocation(latitude, longitude);
    verify(weatherMapper).toWeather(null, null);
  }

  @Test
  @DisplayName("Should handle null dailyWeatherResponse")
  void getWeather_nullDailyWeatherResponse() {
    double latitude = 52.0;
    double longitude = 4.0;
    WeatherResponse hourlyWeatherResponse = mock(WeatherResponse.class);
    ReverseGeocodeResponse reverseGeocodeResponse = mock(ReverseGeocodeResponse.class);
    Weather expectedWeather = mock(Weather.class);
    when(openMeteoClient.getWeatherDaily(latitude, longitude)).thenReturn(null);
    when(openMeteoClient.getWeatherHourly(latitude, longitude)).thenReturn(hourlyWeatherResponse);
    when(reverseGeocodeClient.getLocation(latitude, longitude)).thenReturn(reverseGeocodeResponse);
    when(weatherMapper.toWeather(hourlyWeatherResponse, reverseGeocodeResponse)).thenReturn(
        expectedWeather);
    Weather result = weatherService.getWeather(latitude, longitude);
    assertSame(expectedWeather, result);
    verify(openMeteoClient).getWeatherDaily(latitude, longitude);
    verify(openMeteoClient).getWeatherHourly(latitude, longitude);
    verify(reverseGeocodeClient).getLocation(latitude, longitude);
    verify(weatherMapper).toWeather(hourlyWeatherResponse, reverseGeocodeResponse);
  }

  @Test
  @DisplayName("Should handle null hourlyWeatherResponse")
  void getWeather_nullHourlyWeatherResponse() {
    double latitude = 52.0;
    double longitude = 4.0;
    WeatherResponse dailyWeatherResponse = mock(WeatherResponse.class);
    ReverseGeocodeResponse reverseGeocodeResponse = mock(ReverseGeocodeResponse.class);
    Weather expectedWeather = mock(Weather.class);
    when(openMeteoClient.getWeatherDaily(latitude, longitude)).thenReturn(dailyWeatherResponse);
    when(openMeteoClient.getWeatherHourly(latitude, longitude)).thenReturn(null);
    when(reverseGeocodeClient.getLocation(latitude, longitude)).thenReturn(reverseGeocodeResponse);
    when(weatherMapper.toWeather(null, reverseGeocodeResponse)).thenReturn(expectedWeather);
    Weather result = weatherService.getWeather(latitude, longitude);
    assertSame(expectedWeather, result);
    verify(openMeteoClient).getWeatherDaily(latitude, longitude);
    verify(openMeteoClient).getWeatherHourly(latitude, longitude);
    verify(reverseGeocodeClient).getLocation(latitude, longitude);
    verify(weatherMapper).toWeather(null, reverseGeocodeResponse);
  }

  @Test
  @DisplayName("Should handle null reverseGeocodeResponse")
  void getWeather_nullReverseGeocodeResponse() {
    double latitude = 52.0;
    double longitude = 4.0;
    WeatherResponse dailyWeatherResponse = mock(WeatherResponse.class);
    WeatherResponse hourlyWeatherResponse = mock(WeatherResponse.class);
    Weather expectedWeather = mock(Weather.class);
    when(openMeteoClient.getWeatherDaily(latitude, longitude)).thenReturn(dailyWeatherResponse);
    when(openMeteoClient.getWeatherHourly(latitude, longitude)).thenReturn(hourlyWeatherResponse);
    when(reverseGeocodeClient.getLocation(latitude, longitude)).thenReturn(null);
    when(weatherMapper.toWeather(hourlyWeatherResponse, null)).thenReturn(expectedWeather);
    Weather result = weatherService.getWeather(latitude, longitude);
    assertSame(expectedWeather, result);
    verify(openMeteoClient).getWeatherDaily(latitude, longitude);
    verify(openMeteoClient).getWeatherHourly(latitude, longitude);
    verify(reverseGeocodeClient).getLocation(latitude, longitude);
    verify(weatherMapper).toWeather(hourlyWeatherResponse, null);
  }

  @Test
  @DisplayName("Should handle weatherMapper returning null")
  void getWeather_weatherMapperReturnsNull() {
    double latitude = 52.0;
    double longitude = 4.0;
    WeatherResponse dailyWeatherResponse = mock(WeatherResponse.class);
    WeatherResponse hourlyWeatherResponse = mock(WeatherResponse.class);
    ReverseGeocodeResponse reverseGeocodeResponse = mock(ReverseGeocodeResponse.class);
    when(openMeteoClient.getWeatherDaily(latitude, longitude)).thenReturn(dailyWeatherResponse);
    when(openMeteoClient.getWeatherHourly(latitude, longitude)).thenReturn(hourlyWeatherResponse);
    when(reverseGeocodeClient.getLocation(latitude, longitude)).thenReturn(reverseGeocodeResponse);
    when(weatherMapper.toWeather(hourlyWeatherResponse, reverseGeocodeResponse)).thenReturn(null);
    Weather result = weatherService.getWeather(latitude, longitude);
    assertNull(result);
    verify(openMeteoClient).getWeatherDaily(latitude, longitude);
    verify(openMeteoClient).getWeatherHourly(latitude, longitude);
    verify(reverseGeocodeClient).getLocation(latitude, longitude);
    verify(weatherMapper).toWeather(hourlyWeatherResponse, reverseGeocodeResponse);
  }

  @Test
  @DisplayName("Should search locations and return mapped results")
  void searchLocations_success() {
    String query = "Amsterdam";
    GeocodingResult result1 = mock(GeocodingResult.class);
    GeocodingResult result2 = mock(GeocodingResult.class);
    GeocodingResponse geocodingResponse = new GeocodingResponse(List.of(result1, result2));
    nl.markpost.demo.weather.api.v1.model.Location location1 = mock(
        nl.markpost.demo.weather.api.v1.model.Location.class);
    nl.markpost.demo.weather.api.v1.model.Location location2 = mock(
        nl.markpost.demo.weather.api.v1.model.Location.class);

    when(geocodingClient.searchLocations(query, 5, "en", "json")).thenReturn(geocodingResponse);
    when(geocodingMapper.toLocationDto(result1)).thenReturn(location1);
    when(geocodingMapper.toLocationDto(result2)).thenReturn(location2);

    List<nl.markpost.demo.weather.api.v1.model.Location> result = weatherService.searchLocations(
        query);

    assertEquals(2, result.size());
    assertSame(location1, result.get(0));
    assertSame(location2, result.get(1));
    verify(geocodingClient).searchLocations(query, 5, "en", "json");
    verify(geocodingMapper).toLocationDto(result1);
    verify(geocodingMapper).toLocationDto(result2);
  }

  @Test
  @DisplayName("Should return empty list when search query is null")
  void searchLocations_nullQuery() {
    List<nl.markpost.demo.weather.api.v1.model.Location> result = weatherService.searchLocations(
        null);

    assertTrue(result.isEmpty());
    verifyNoInteractions(geocodingClient);
    verifyNoInteractions(geocodingMapper);
  }

  @Test
  @DisplayName("Should return empty list when search query is empty")
  void searchLocations_emptyQuery() {
    List<nl.markpost.demo.weather.api.v1.model.Location> result = weatherService.searchLocations(
        "  ");

    assertTrue(result.isEmpty());
    verifyNoInteractions(geocodingClient);
    verifyNoInteractions(geocodingMapper);
  }

  @Test
  @DisplayName("Should return empty list when geocoding response is null")
  void searchLocations_nullResponse() {
    String query = "Amsterdam";
    when(geocodingClient.searchLocations(query, 5, "en", "json")).thenReturn(null);

    List<nl.markpost.demo.weather.api.v1.model.Location> result = weatherService.searchLocations(
        query);

    assertTrue(result.isEmpty());
    verify(geocodingClient).searchLocations(query, 5, "en", "json");
    verifyNoInteractions(geocodingMapper);
  }

  @Test
  @DisplayName("Should return empty list when geocoding response has null results")
  void searchLocations_nullResultsList() {
    String query = "Amsterdam";
    GeocodingResponse geocodingResponse = new GeocodingResponse(null);
    when(geocodingClient.searchLocations(query, 5, "en", "json")).thenReturn(geocodingResponse);

    List<nl.markpost.demo.weather.api.v1.model.Location> result = weatherService.searchLocations(
        query);

    assertTrue(result.isEmpty());
    verify(geocodingClient).searchLocations(query, 5, "en", "json");
    verifyNoInteractions(geocodingMapper);
  }

  @Test
  @DisplayName("Should return empty list when geocoding response has empty results")
  void searchLocations_emptyResultsList() {
    String query = "Amsterdam";
    GeocodingResponse geocodingResponse = new GeocodingResponse(Collections.emptyList());
    when(geocodingClient.searchLocations(query, 5, "en", "json")).thenReturn(geocodingResponse);

    List<nl.markpost.demo.weather.api.v1.model.Location> result = weatherService.searchLocations(
        query);

    assertTrue(result.isEmpty());
    verify(geocodingClient).searchLocations(query, 5, "en", "json");
    verifyNoInteractions(geocodingMapper);
  }
}