package nl.markpost.demo.weather.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import nl.markpost.demo.weather.model.Hourly;
import nl.markpost.demo.weather.model.HourlyResponse;
import nl.markpost.demo.weather.model.Weather;
import nl.markpost.demo.weather.model.WeatherResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = CurrentMapper.class)
public interface WeatherMapper {

    @Mappings({
        @Mapping(source = "latitude", target = "latitude"),
        @Mapping(source = "longitude", target = "longitude"),
        @Mapping(source = "timezone", target = "timezone"),
        @Mapping(source = "elevation", target = "elevation"),
        @Mapping(source = "current", target = "current"),
        @Mapping(target = "daily", expression = "java(toDailyList(response.getDaily()))")
    })
    Weather toWeather(WeatherResponse response);

    default LocalDateTime mapTime(String value) {
        if (value == null) return null;
        return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
    }

    default List<nl.markpost.demo.weather.model.Daily> toDailyList(nl.markpost.demo.weather.model.DailyResponse daily) {
        if (daily == null) return null;
        List<String> times = daily.getTime();
        List<Integer> codes = daily.getWeather_code();
        List<Double> tempMax = daily.getTemperature_2m_max();
        List<Double> tempMin = daily.getTemperature_2m_min();
        List<String> sunRises = daily.getSunrise();
        List<String> sunSets = daily.getSunset();
        List<Double> precips = daily.getPrecipitation_sum();
        List<nl.markpost.demo.weather.model.Daily> result = new ArrayList<>();
        int size = times != null ? times.size() : 0;
        for (int i = 0; i < size; i++) {
            LocalDateTime time = i < times.size() ? mapDate(times.get(i)) : null;
            LocalDateTime sunRise = sunRises != null && i < sunRises.size() ? mapDateTime(sunRises.get(i)) : null;
            LocalDateTime sunSet = sunSets != null && i < sunSets.size() ? mapDateTime(sunSets.get(i)) : null;
            nl.markpost.demo.weather.model.WeatherCode weatherCode = codes != null && i < codes.size() ? nl.markpost.demo.weather.model.WeatherCode.fromCode(codes.get(i)) : nl.markpost.demo.weather.model.WeatherCode.CLEAR_SKY;
            double temperatureMin = tempMin != null && i < tempMin.size() ? tempMin.get(i) : 0.0;
            double temperatureMax = tempMax != null && i < tempMax.size() ? tempMax.get(i) : 0.0;
            int precipitation = precips != null && i < precips.size() ? (int)Math.round(precips.get(i)) : 0;
            result.add(new nl.markpost.demo.weather.model.Daily(time, sunRise, sunSet, weatherCode, temperatureMin, temperatureMax, precipitation));
        }
        return result;
    }

    default LocalDateTime mapDate(String value) {
        if (value == null) return null;
        return LocalDateTime.parse(value + "T00:00:00");
    }

    default LocalDateTime mapDateTime(String value) {
        if (value == null) return null;
        return LocalDateTime.parse(value);
    }
}
