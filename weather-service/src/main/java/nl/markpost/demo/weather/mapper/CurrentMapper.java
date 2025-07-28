package nl.markpost.demo.weather.mapper;

import nl.markpost.demo.weather.model.Current;
import nl.markpost.demo.weather.model.CurrentResponse;
import nl.markpost.demo.weather.model.WeatherResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper interface for converting CurrentResponse to Current model.
 * This mapper uses MapStruct to automatically generate the implementation.
 */
@Mapper(componentModel = "spring")
public interface CurrentMapper {

    /**
     * Converts a CurrentResponse object to a Current model.
     *
     * @param current the CurrentResponse object to convert
     * @return the converted Current model
     */
    @Mapping(target = "time", expression = "java(java.time.LocalDateTime.parse(current.getTime()))")
    @Mapping(target = "weatherCode", expression = "java(nl.markpost.demo.weather.model.WeatherCode.fromCode(current.getWeather_code()))")
    @Mapping(source = "temperature_2m", target = "temperature")
    @Mapping(source = "wind_speed_10m", target = "windSpeed")
    @Mapping(source = "wind_direction_10m", target = "windDirection")
    Current toCurrent(CurrentResponse current);

}
