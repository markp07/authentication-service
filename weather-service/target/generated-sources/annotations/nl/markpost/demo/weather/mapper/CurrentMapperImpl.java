package nl.markpost.demo.weather.mapper;

import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import nl.markpost.demo.weather.model.Current;
import nl.markpost.demo.weather.model.CurrentResponse;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-17T23:28:17+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Azul Systems, Inc.)"
)
@Component
public class CurrentMapperImpl implements CurrentMapper {

    @Override
    public Current toCurrent(CurrentResponse current) {
        if ( current == null ) {
            return null;
        }

        Current current1 = new Current();

        if ( current.getTime() != null ) {
            current1.setTime( LocalDateTime.parse( current.getTime() ) );
        }
        current1.setTemperature( current.getTemperature_2m() );
        if ( current.getRelative_humidity_2m() != null ) {
            current1.setRelativeHumidity( current.getRelative_humidity_2m() );
        }
        current1.setWindSpeed( current.getWind_speed_10m() );

        return current1;
    }
}
