package nl.markpost.demo.weather.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import nl.markpost.demo.weather.model.Current;
import nl.markpost.demo.weather.model.Daily;
import nl.markpost.demo.weather.model.Hourly;
import nl.markpost.demo.weather.model.Weather;
import nl.markpost.demo.weather.model.WeatherCode;
import nl.markpost.demo.weather.model.WindDirection;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

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
  nl.markpost.demo.weather.api.v1.model.Weather toApiModel(Weather weather);

  /**
   * Maps domain Current model to API Current model.
   *
   * @param current domain model
   * @return API model
   */
  nl.markpost.demo.weather.api.v1.model.Current toApiModel(Current current);

  /**
   * Maps domain Daily model to API Daily model.
   *
   * @param daily domain model
   * @return API model
   */
  nl.markpost.demo.weather.api.v1.model.Daily toApiModel(Daily daily);

  /**
   * Maps domain Hourly model to API Hourly model.
   *
   * @param hourly domain model
   * @return API model
   */
  nl.markpost.demo.weather.api.v1.model.Hourly toApiModel(Hourly hourly);

  /**
   * Converts LocalDateTime to String.
   *
   * @param dateTime LocalDateTime
   * @return String representation
   */
  @Named("localDateTimeToString")
  default String localDateTimeToString(LocalDateTime dateTime) {
    return dateTime != null ? dateTime.format(FORMATTER) : null;
  }

  /**
   * Converts WeatherCode enum to string.
   *
   * @param weatherCode domain enum
   * @return API enum string
   */
  default nl.markpost.demo.weather.api.v1.model.Current.WeatherCodeEnum mapWeatherCodeToCurrent(WeatherCode weatherCode) {
    return weatherCode != null ? nl.markpost.demo.weather.api.v1.model.Current.WeatherCodeEnum.fromValue(weatherCode.name()) : null;
  }

  /**
   * Converts WeatherCode enum to string for Daily.
   *
   * @param weatherCode domain enum
   * @return API enum string
   */
  default nl.markpost.demo.weather.api.v1.model.Daily.WeatherCodeEnum mapWeatherCodeToDaily(WeatherCode weatherCode) {
    return weatherCode != null ? nl.markpost.demo.weather.api.v1.model.Daily.WeatherCodeEnum.fromValue(weatherCode.name()) : null;
  }

  /**
   * Converts WeatherCode enum to string for Hourly.
   *
   * @param weatherCode domain enum
   * @return API enum string
   */
  default nl.markpost.demo.weather.api.v1.model.Hourly.WeatherCodeEnum mapWeatherCodeToHourly(WeatherCode weatherCode) {
    return weatherCode != null ? nl.markpost.demo.weather.api.v1.model.Hourly.WeatherCodeEnum.fromValue(weatherCode.name()) : null;
  }

  /**
   * Converts WindDirection enum to string.
   *
   * @param windDirection domain enum
   * @return API enum string
   */
  default nl.markpost.demo.weather.api.v1.model.Current.WindDirectionEnum mapWindDirectionToCurrent(WindDirection windDirection) {
    return windDirection != null ? nl.markpost.demo.weather.api.v1.model.Current.WindDirectionEnum.fromValue(windDirection.name()) : null;
  }

  /**
   * Converts WindDirection enum to string for Daily.
   *
   * @param windDirection domain enum
   * @return API enum string
   */
  default nl.markpost.demo.weather.api.v1.model.Daily.WindDirectionEnum mapWindDirectionToDaily(WindDirection windDirection) {
    return windDirection != null ? nl.markpost.demo.weather.api.v1.model.Daily.WindDirectionEnum.fromValue(windDirection.name()) : null;
  }

  /**
   * Converts WindDirection enum to string for Hourly.
   *
   * @param windDirection domain enum
   * @return API enum string
   */
  default nl.markpost.demo.weather.api.v1.model.Hourly.WindDirectionEnum mapWindDirectionToHourly(WindDirection windDirection) {
    return windDirection != null ? nl.markpost.demo.weather.api.v1.model.Hourly.WindDirectionEnum.fromValue(windDirection.name()) : null;
  }
}
