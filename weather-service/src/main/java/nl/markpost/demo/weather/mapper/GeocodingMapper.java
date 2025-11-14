package nl.markpost.demo.weather.mapper;

import nl.markpost.demo.weather.api.v1.model.Location;
import nl.markpost.demo.weather.model.GeocodingResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * Mapper interface for converting GeocodingResult to Location DTO. This mapper uses MapStruct to
 * automatically generate the implementation.
 */
@Mapper(componentModel = "spring")
public interface GeocodingMapper {

  /**
   * Maps a GeocodingResult object to a Location DTO.
   */
  @Mappings({
      @Mapping(target = "id", source = "id"),
      @Mapping(target = "name", source = "name"),
      @Mapping(target = "latitude", source = "latitude"),
      @Mapping(target = "longitude", source = "longitude"),
      @Mapping(target = "country", source = "country"),
      @Mapping(target = "countryCode", source = "countryCode"),
      @Mapping(target = "admin1", source = "admin1"),
      @Mapping(target = "timezone", source = "timezone")
  })
  Location toLocationDto(GeocodingResult result);
}
