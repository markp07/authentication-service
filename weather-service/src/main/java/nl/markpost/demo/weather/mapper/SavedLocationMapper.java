package nl.markpost.demo.weather.mapper;

import nl.markpost.demo.weather.api.v1.model.Location;
import nl.markpost.demo.weather.entity.SavedLocation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting between SavedLocation entity and Location API model.
 */
@Mapper(componentModel = "spring")
public interface SavedLocationMapper {

  /**
   * Maps SavedLocation entity to Location API model.
   *
   * @param savedLocation the saved location entity
   * @return location API model
   */
  @Mapping(source = "locationId", target = "id")
  Location toApiModel(SavedLocation savedLocation);

  /**
   * Maps Location API model to SavedLocation entity.
   *
   * @param location the location API model
   * @return saved location entity
   */
  @Mapping(source = "id", target = "locationId")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "displayOrder", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  SavedLocation toEntity(Location location);
}
