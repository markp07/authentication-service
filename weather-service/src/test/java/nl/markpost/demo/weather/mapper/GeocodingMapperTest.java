package nl.markpost.demo.weather.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import nl.markpost.demo.weather.api.v1.model.Location;
import nl.markpost.demo.weather.model.GeocodingResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class GeocodingMapperTest {

  private final GeocodingMapper mapper = Mappers.getMapper(GeocodingMapper.class);

  @Test
  @DisplayName("Should map GeocodingResult to Location DTO")
  void toLocationDto_success() {
    GeocodingResult result = new GeocodingResult();
    result.setId(12345L);
    result.setName("Amsterdam");
    result.setLatitude(52.3676);
    result.setLongitude(4.9041);
    result.setCountry("Netherlands");
    result.setCountryCode("NL");
    result.setAdmin1("North Holland");
    result.setTimezone("Europe/Amsterdam");

    Location location = mapper.toLocationDto(result);

    assertNotNull(location);
    assertEquals(12345L, location.getId());
    assertEquals("Amsterdam", location.getName());
    assertEquals(52.3676, location.getLatitude());
    assertEquals(4.9041, location.getLongitude());
    assertEquals("Netherlands", location.getCountry());
    assertEquals("NL", location.getCountryCode());
    assertEquals("North Holland", location.getAdmin1());
    assertEquals("Europe/Amsterdam", location.getTimezone());
  }

  @Test
  @DisplayName("Should handle null GeocodingResult")
  void toLocationDto_nullInput() {
    Location location = mapper.toLocationDto(null);
    assertNull(location);
  }

  @Test
  @DisplayName("Should handle GeocodingResult with null fields")
  void toLocationDto_nullFields() {
    GeocodingResult result = new GeocodingResult();
    result.setId(1L);
    result.setName("Test");
    result.setLatitude(0.0);
    result.setLongitude(0.0);

    Location location = mapper.toLocationDto(result);

    assertNotNull(location);
    assertEquals(1L, location.getId());
    assertEquals("Test", location.getName());
    assertNull(location.getCountry());
    assertNull(location.getCountryCode());
    assertNull(location.getAdmin1());
    assertNull(location.getTimezone());
  }
}
