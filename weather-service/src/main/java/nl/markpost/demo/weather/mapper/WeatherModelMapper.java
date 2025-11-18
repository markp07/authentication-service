package nl.markpost.demo.weather.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.mapstruct.Mapper;

/**
 * Mapper for converting between domain models and API models.
 */
@Mapper(componentModel = "spring")
public interface WeatherModelMapper {

  DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  /**
   * Maps domain Weather model to API Weather model.
   *
   * @param weather domain model
   * @return API model
   */
  WeatherResponse from(Weather weather);

  /**
   * Maps domain Current model to API Current model.
   *
   * @param current domain model
   * @return API model
   */
  CurrentResponse from(Current current);

  /**
   * Maps domain Daily model to API Daily model.
   *
   * @param daily domain model
   * @return API model
   */
  DailyResponse from(Daily daily);

  /**
   * Maps domain Hourly model to API Hourly model.
   *
   * @param hourly domain model
   * @return API model
   */
  HourlyResponse from(Hourly hourly);

  /**
   * Converts LocalDateTime to String.
   *
   * @param dateTime LocalDateTime
   * @return String representation
   */
  default String map(LocalDateTime dateTime) {
    return dateTime != null ? dateTime.format(FORMATTER) : null;
  }

  /**
   * Converts WeatherCode enum to string.
   *
   * @param weatherCode domain enum
   * @return API enum string
   */
  default WeatherCodeResponse map(
      WeatherCode weatherCode) {
    return weatherCode != null
        ? WeatherCodeResponse.fromValue(weatherCode.name()) : null;
  }

  /**
   * Converts WindDirection enum to string.
   *
   * @param windDirection domain enum
   * @return API enum string
   */
  default WindDirectionResponse map(
      WindDirection windDirection) {
    return windDirection != null
        ? WindDirectionResponse.fromValue(
        windDirection.name()) : null;
  }

}
