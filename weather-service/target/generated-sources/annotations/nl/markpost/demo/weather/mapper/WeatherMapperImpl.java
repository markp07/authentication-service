package nl.markpost.demo.weather.mapper;

import javax.annotation.processing.Generated;
import nl.markpost.demo.weather.model.Weather;
import nl.markpost.demo.weather.model.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-17T23:28:17+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Azul Systems, Inc.)"
)
@Component
public class WeatherMapperImpl implements WeatherMapper {

    @Autowired
    private CurrentMapper currentMapper;

    @Override
    public Weather toWeather(WeatherResponse response) {
        if ( response == null ) {
            return null;
        }

        Weather weather = new Weather();

        weather.setLatitude( response.getLatitude() );
        weather.setLongitude( response.getLongitude() );
        weather.setTimezone( response.getTimezone() );
        weather.setElevation( response.getElevation() );
        weather.setCurrent( currentMapper.toCurrent( response.getCurrent() ) );
        weather.setHourly( toHourlyList( response.getHourly() ) );

        return weather;
    }
}
