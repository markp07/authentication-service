package nl.markpost.demo.weather.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import nl.markpost.demo.weather.model.Daily;
import nl.markpost.demo.weather.model.DailyResponse;
import nl.markpost.demo.weather.model.Hourly;
import nl.markpost.demo.weather.model.HourlyResponse;
import nl.markpost.demo.weather.model.ReverseGeocodeResponse;
import nl.markpost.demo.weather.model.Weather;
import nl.markpost.demo.weather.model.WeatherCode;
import nl.markpost.demo.weather.model.WeatherResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Mapper interface for converting WeatherResponse to Weather model. This mapper uses MapStruct to
 * automatically generate the implementation.
 */
@Mapper(componentModel = "spring", uses = CurrentMapper.class)
public interface WeatherMapper {

  /**
   * Maps a WeatherResponse object to a Weather model.
   */
  @Mappings({
      @Mapping(target = "latitude", source = "weather.latitude"),
      @Mapping(target = "longitude", source = "weather.longitude"),
      @Mapping(target = "timezone", source = "weather.timezone"),
      @Mapping(target = "elevation", source = "weather.elevation"),
      @Mapping(target = "current", source = "weather.current"),
      @Mapping(target = "location", source = "location.city"),
      @Mapping(target = "daily", expression = "java(toDailyList(weather.getDaily()))"),
      @Mapping(target = "hourly", expression = "java(toHourlyList(weather.getHourly()))")
  })
  Weather toWeather(WeatherResponse weather, ReverseGeocodeResponse location);

  /**
   * Maps a date or date-time string to a LocalDateTime object. Accepts either ISO date (e.g.,
   * "2023-10-01") or ISO date-time (e.g., "2023-10-01T12:00:00"). If only a date is provided, time
   * is set to midnight.
   *
   * @param value the date or date-time string in ISO format
   * @return the LocalDateTime object representing the specified date and time
   */
  default LocalDateTime mapToLocalDateTime(String value) {
    if (value == null) {
      return null;
    }
    if (value.length() == 10) { // e.g. "2023-10-01"
      return LocalDateTime.parse(value + "T00:00:00");
    }
    return LocalDateTime.parse(value);
  }

  /**
   * Converts a DailyResponse object to a List of Daily models.
   *
   * @param daily the DailyResponse object to convert
   * @return the converted List of Daily models
   */
  default List<Daily> toDailyList(DailyResponse daily) {
    if (daily == null) {
      return null;
    }
    List<String> times = daily.getTime();
    List<Integer> codes = daily.getWeather_code();
    List<Double> tempMax = daily.getTemperature_2m_max();
    List<Double> tempMin = daily.getTemperature_2m_min();
    List<String> sunRises = daily.getSunrise();
    List<String> sunSets = daily.getSunset();
    List<Double> precips = daily.getPrecipitation_sum();
    List<Integer> precipProbMax = daily.getPrecipitation_probability_max();
    List<Daily> result = new ArrayList<>();
    int size = times != null ? times.size() : 0;
    for (int i = 0; i < size; i++) {
      LocalDateTime time = i < times.size() ? mapToLocalDateTime(times.get(i)) : null;
      WeatherCode weatherCode =
          codes != null && i < codes.size() ? WeatherCode.fromCode(codes.get(i))
              : WeatherCode.CLEAR_SKY;
      double temperatureMin = tempMin != null && i < tempMin.size() ? tempMin.get(i) : 0.0;
      double temperatureMax = tempMax != null && i < tempMax.size() ? tempMax.get(i) : 0.0;
      double precipitation = precips != null && i < precips.size() ? precips.get(i) : 0.0;
      int precipitationProbabilityMax =
          precipProbMax != null && i < precipProbMax.size() ? precipProbMax.get(i) : 0;
      LocalDateTime sunRise =
          sunRises != null && i < sunRises.size() ? mapToLocalDateTime(sunRises.get(i)) : null;
      LocalDateTime sunSet =
          sunSets != null && i < sunSets.size() ? mapToLocalDateTime(sunSets.get(i)) : null;
      result.add(new Daily(time, weatherCode, temperatureMin, temperatureMax, precipitation,
          precipitationProbabilityMax, sunRise, sunSet));
    }
    return result;
  }

  /**
   * Converts a HourlyResponse object to a List of Hourly models. Skips hours before current hour,
   * then adds next 48 hours.
   *
   * @param hourly the HourlyResponse object to convert
   * @return the converted List of Hourly models
   */
  default List<Hourly> toHourlyList(HourlyResponse hourly) {
    if (hourly == null) {
      return null;
    }
    List<String> times = hourly.getTime();
    List<Integer> codes = hourly.getWeather_code();
    List<Double> temps = hourly.getTemperature_2m();
    List<Integer> precipProbs = hourly.getPrecipitation_probability();
    List<Hourly> result = new ArrayList<>();
    int size = times != null ? times.size() : 0;
    LocalDateTime now = LocalDateTime.now();
    int startIdx = 0;
    // Find first index where time >= now
    for (int i = 0; i < size; i++) {
      LocalDateTime time = mapToLocalDateTime(times.get(i));
      if (!time.isBefore(now)) {
        startIdx = i + 1;
        break;
      }
    }
    int endIdx = Math.min(startIdx + 48, size);
    for (int i = startIdx; i < endIdx; i++) {
      LocalDateTime time = i < times.size() ? mapToLocalDateTime(times.get(i)) : null;
      WeatherCode weatherCode =
          codes != null && i < codes.size() ? WeatherCode.fromCode(codes.get(i))
              : WeatherCode.CLEAR_SKY;
      double temperature = temps != null && i < temps.size() ? temps.get(i) : 0.0;
      int precipitationProbability =
          precipProbs != null && i < precipProbs.size() ? precipProbs.get(i) : 0;
      result.add(new Hourly(time, weatherCode, temperature, precipitationProbability));
    }
    return result;
  }
}
