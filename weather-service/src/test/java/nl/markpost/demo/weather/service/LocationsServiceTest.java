package nl.markpost.demo.weather.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.markpost.demo.weather.api.v1.model.Location;
import nl.markpost.demo.weather.client.GeocodingClient;
import nl.markpost.demo.weather.entity.SavedLocation;
import nl.markpost.demo.weather.mapper.GeocodingMapper;
import nl.markpost.demo.weather.mapper.SavedLocationMapper;
import nl.markpost.demo.weather.model.GeocodingResponse;
import nl.markpost.demo.weather.model.GeocodingResult;
import nl.markpost.demo.weather.repository.SavedLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationsServiceTest {

  @Mock
  private SavedLocationRepository savedLocationRepository;

  @Mock
  private SavedLocationMapper savedLocationMapper;

  @Mock
  private GeocodingClient geocodingClient;

  @Mock
  private GeocodingMapper geocodingMapper;

  @InjectMocks
  private LocationsService locationsService;

  private UUID userId;
  private SavedLocation savedLocation;
  private Location locationDto;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();

    savedLocation = SavedLocation.builder()
        .id(1L)
        .userId(userId)
        .locationId(123L)
        .name("Amsterdam")
        .latitude(52.3676)
        .longitude(4.9041)
        .country("Netherlands")
        .countryCode("NL")
        .admin1("North Holland")
        .timezone("Europe/Amsterdam")
        .build();

    locationDto = new Location();
    locationDto.setId(123L);
    locationDto.setName("Amsterdam");
    locationDto.setLatitude(52.3676);
    locationDto.setLongitude(4.9041);
    locationDto.setCountry("Netherlands");
    locationDto.setCountryCode("NL");
    locationDto.setAdmin1("North Holland");
    locationDto.setTimezone("Europe/Amsterdam");
  }

  @Test
  void testGetSavedLocations() {
    when(savedLocationRepository.findByUserIdOrderByDisplayOrderAsc(userId)).thenReturn(
        Collections.singletonList(savedLocation));
    when(savedLocationMapper.toApiModel(savedLocation)).thenReturn(locationDto);

    List<Location> result = locationsService.getSavedLocations(
        userId);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Amsterdam", result.getFirst().getName());
    assertEquals(52.3676, result.getFirst().getLatitude());
    verify(savedLocationRepository, times(1)).findByUserIdOrderByDisplayOrderAsc(userId);
    verify(savedLocationMapper, times(1)).toApiModel(savedLocation);
  }

  @Test
  void testSaveLocation_NewLocation() {
    when(savedLocationRepository.findByUserIdAndLocationId(userId, 123L)).thenReturn(
        Optional.empty());
    when(savedLocationRepository.findByUserIdOrderByDisplayOrderAsc(userId)).thenReturn(
        Collections.emptyList());
    when(savedLocationMapper.toEntity(locationDto)).thenReturn(savedLocation);
    when(savedLocationRepository.save(any(SavedLocation.class))).thenReturn(savedLocation);

    Location result = locationsService.saveLocation(
        userId, locationDto);

    assertNotNull(result);
    assertEquals("Amsterdam", result.getName());
    verify(savedLocationMapper, times(1)).toEntity(locationDto);
    verify(savedLocationRepository, times(1)).save(any(SavedLocation.class));
  }

  @Test
  void testSaveLocation_AlreadyExists() {
    when(savedLocationRepository.findByUserIdAndLocationId(userId, 123L)).thenReturn(
        Optional.of(savedLocation));

    Location result = locationsService.saveLocation(
        userId, locationDto);

    assertNotNull(result);
    assertEquals("Amsterdam", result.getName());
    verify(savedLocationMapper, never()).toEntity(any());
    verify(savedLocationRepository, never()).save(any(SavedLocation.class));
  }

  @Test
  void testDeleteSavedLocation() {
    locationsService.deleteSavedLocation(1L, userId);

    verify(savedLocationRepository, times(1)).deleteByLocationIdAndUserId(1L, userId);
  }

  @Test
  @DisplayName("Should reorder saved locations")
  void testReorderLocations() {
    SavedLocation location1 = SavedLocation.builder()
        .id(1L)
        .userId(userId)
        .locationId(100L)
        .name("Location 1")
        .displayOrder(0)
        .build();
    SavedLocation location2 = SavedLocation.builder()
        .id(2L)
        .userId(userId)
        .locationId(200L)
        .name("Location 2")
        .displayOrder(1)
        .build();
    SavedLocation location3 = SavedLocation.builder()
        .id(3L)
        .userId(userId)
        .locationId(300L)
        .name("Location 3")
        .displayOrder(2)
        .build();

    List<SavedLocation> savedLocations = List.of(location1, location2, location3);
    when(savedLocationRepository.findByUserIdOrderByDisplayOrderAsc(userId)).thenReturn(savedLocations);

    // Reorder: [300, 100, 200]
    List<Long> newOrder = List.of(300L, 100L, 200L);
    locationsService.reorderLocations(userId, newOrder);

    verify(savedLocationRepository, times(3)).save(any(SavedLocation.class));
    assertEquals(1, location1.getDisplayOrder()); // 100L is now at position 1
    assertEquals(2, location2.getDisplayOrder()); // 200L is now at position 2
    assertEquals(0, location3.getDisplayOrder()); // 300L is now at position 0
  }

  @Test
  @DisplayName("Should search locations and return mapped results")
  void searchLocations_success() {
    String query = "Amsterdam";
    GeocodingResult result1 = mock(GeocodingResult.class);
    GeocodingResult result2 = mock(GeocodingResult.class);
    GeocodingResponse geocodingResponse = new GeocodingResponse(List.of(result1, result2));
    Location location1 = mock(Location.class);
    Location location2 = mock(Location.class);

    when(geocodingClient.searchLocations(query, 5, "en", "json")).thenReturn(geocodingResponse);
    when(geocodingMapper.toLocationDto(result1)).thenReturn(location1);
    when(geocodingMapper.toLocationDto(result2)).thenReturn(location2);

    List<Location> result = locationsService.searchLocations(
        query);

    assertEquals(2, result.size());
    assertSame(location1, result.get(0));
    assertSame(location2, result.get(1));
    verify(geocodingClient).searchLocations(query, 5, "en", "json");
    verify(geocodingMapper).toLocationDto(result1);
    verify(geocodingMapper).toLocationDto(result2);
  }

  @Test
  @DisplayName("Should return empty list when search query is null")
  void searchLocations_nullQuery() {
    List<Location> result = locationsService.searchLocations(
        null);

    assertTrue(result.isEmpty());
    verifyNoInteractions(geocodingClient);
    verifyNoInteractions(geocodingMapper);
  }

  @Test
  @DisplayName("Should return empty list when search query is empty")
  void searchLocations_emptyQuery() {
    List<Location> result = locationsService.searchLocations(
        "  ");

    assertTrue(result.isEmpty());
    verifyNoInteractions(geocodingClient);
    verifyNoInteractions(geocodingMapper);
  }

  @Test
  @DisplayName("Should return empty list when geocoding response is null")
  void searchLocations_nullResponse() {
    String query = "Amsterdam";
    when(geocodingClient.searchLocations(query, 5, "en", "json")).thenReturn(null);

    List<Location> result = locationsService.searchLocations(
        query);

    assertTrue(result.isEmpty());
    verify(geocodingClient).searchLocations(query, 5, "en", "json");
    verifyNoInteractions(geocodingMapper);
  }

  @Test
  @DisplayName("Should return empty list when geocoding response has null results")
  void searchLocations_nullResultsList() {
    String query = "Amsterdam";
    GeocodingResponse geocodingResponse = new GeocodingResponse(null);
    when(geocodingClient.searchLocations(query, 5, "en", "json")).thenReturn(geocodingResponse);

    List<Location> result = locationsService.searchLocations(
        query);

    assertTrue(result.isEmpty());
    verify(geocodingClient).searchLocations(query, 5, "en", "json");
    verifyNoInteractions(geocodingMapper);
  }

  @Test
  @DisplayName("Should return empty list when geocoding response has empty results")
  void searchLocations_emptyResultsList() {
    String query = "Amsterdam";
    GeocodingResponse geocodingResponse = new GeocodingResponse(Collections.emptyList());
    when(geocodingClient.searchLocations(query, 5, "en", "json")).thenReturn(geocodingResponse);

    List<Location> result = locationsService.searchLocations(
        query);

    assertTrue(result.isEmpty());
    verify(geocodingClient).searchLocations(query, 5, "en", "json");
    verifyNoInteractions(geocodingMapper);
  }
}
