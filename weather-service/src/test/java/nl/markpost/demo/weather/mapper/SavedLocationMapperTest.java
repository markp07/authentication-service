package nl.markpost.demo.weather.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import nl.markpost.demo.weather.api.v1.model.Location;
import nl.markpost.demo.weather.entity.SavedLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class SavedLocationMapperTest {

  private final SavedLocationMapper mapper = Mappers.getMapper(SavedLocationMapper.class);

  @Test
  @DisplayName("Should map SavedLocation to Location API model")
  void toApiModel_success() {
    SavedLocation savedLocation = SavedLocation.builder()
        .id(1L)
        .locationId(12345L)
        .name("Amsterdam")
        .latitude(52.3676)
        .longitude(4.9041)
        .country("Netherlands")
        .countryCode("NL")
        .admin1("North Holland")
        .timezone("Europe/Amsterdam")
        .build();

    Location location = mapper.toApiModel(savedLocation);

    assertNotNull(location);
    assertEquals(12345L, location.getId()); // locationId maps to id
    assertEquals("Amsterdam", location.getName());
    assertEquals(52.3676, location.getLatitude());
    assertEquals(4.9041, location.getLongitude());
    assertEquals("Netherlands", location.getCountry());
    assertEquals("NL", location.getCountryCode());
    assertEquals("North Holland", location.getAdmin1());
    assertEquals("Europe/Amsterdam", location.getTimezone());
  }

  @Test
  @DisplayName("Should handle null SavedLocation")
  void toApiModel_nullInput() {
    Location location = mapper.toApiModel(null);
    assertNull(location);
  }

  @Test
  @DisplayName("Should map Location API model to SavedLocation entity")
  void toEntity_success() {
    Location location = new Location();
    location.setId(12345L);
    location.setName("Amsterdam");
    location.setLatitude(52.3676);
    location.setLongitude(4.9041);
    location.setCountry("Netherlands");
    location.setCountryCode("NL");
    location.setAdmin1("North Holland");
    location.setTimezone("Europe/Amsterdam");

    SavedLocation savedLocation = mapper.toEntity(location);

    assertNotNull(savedLocation);
    assertNull(savedLocation.getId()); // id is ignored
    assertEquals(12345L, savedLocation.getLocationId()); // id maps to locationId
    assertEquals("Amsterdam", savedLocation.getName());
    assertEquals(52.3676, savedLocation.getLatitude());
    assertEquals(4.9041, savedLocation.getLongitude());
    assertEquals("Netherlands", savedLocation.getCountry());
    assertEquals("NL", savedLocation.getCountryCode());
    assertEquals("North Holland", savedLocation.getAdmin1());
    assertEquals("Europe/Amsterdam", savedLocation.getTimezone());
    assertNull(savedLocation.getUserId()); // ignored
    assertNull(savedLocation.getDisplayOrder()); // ignored
  }

  @Test
  @DisplayName("Should handle null Location")
  void toEntity_nullInput() {
    SavedLocation savedLocation = mapper.toEntity(null);
    assertNull(savedLocation);
  }

  @Test
  @DisplayName("Should handle SavedLocation with null optional fields")
  void toApiModel_nullOptionalFields() {
    SavedLocation savedLocation = SavedLocation.builder()
        .id(1L)
        .locationId(12345L)
        .name("Test")
        .latitude(0.0)
        .longitude(0.0)
        .build();

    Location location = mapper.toApiModel(savedLocation);

    assertNotNull(location);
    assertEquals(12345L, location.getId());
    assertEquals("Test", location.getName());
    assertNull(location.getCountry());
    assertNull(location.getCountryCode());
  }
}
