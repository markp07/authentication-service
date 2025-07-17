package nl.markpost.demo.weather.mapper;

import nl.markpost.demo.weather.model.Current;
import nl.markpost.demo.weather.model.CurrentResponse;
import nl.markpost.demo.weather.model.WeatherResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CurrentMapper {
    @Mapping(source = "time", target = "time")
    @Mapping(source = "temperature_2m", target = "temperature")
    @Mapping(source = "relative_humidity_2m", target = "relativeHumidity")
    @Mapping(source = "wind_speed_10m", target = "windSpeed")
    Current toCurrent(CurrentResponse current);
}

