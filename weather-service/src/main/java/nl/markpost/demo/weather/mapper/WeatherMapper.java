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
    WeatherMapper INSTANCE = Mappers.getMapper(WeatherMapper.class);

    @Mappings({
        @Mapping(source = "latitude", target = "latitude"),
        @Mapping(source = "longitude", target = "longitude"),
        @Mapping(source = "timezone", target = "timezone"),
        @Mapping(source = "elevation", target = "elevation"),
        @Mapping(source = "current", target = "current"),
        @Mapping(source = "hourly", target = "hourly")
    })
    Weather toWeather(WeatherResponse response);

    default LocalDateTime mapTime(String value) {
        if (value == null) return null;
        return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
    }

    default List<Hourly> toHourlyList(HourlyResponse hourly) {
        if (hourly == null) {
            return null;
        }
        List<String> times = hourly.getTime();
        List<Double> temps = hourly.getTemperature_2m();
        List<Integer> hums = hourly.getRelative_humidity_2m();
        List<Double> winds = hourly.getWind_speed_10m();
        List<Hourly> result = new ArrayList<>();
        int size = times != null ? times.size() : 0;
        for (int i = 0; i < size; i++) {
            LocalDateTime time = i < times.size() ? mapTime(times.get(i)) : null;
            double temp = temps != null && i < temps.size() ? temps.get(i) : 0.0;
            int hum = hums != null && i < hums.size() ? hums.get(i) : 0;
            double wind = winds != null && i < winds.size() ? winds.get(i) : 0.0;
            result.add(new Hourly(time, temp, wind, hum));
        }
        return result;
    }
}
