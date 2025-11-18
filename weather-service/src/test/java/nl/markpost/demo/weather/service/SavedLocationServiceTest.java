package nl.markpost.demo.weather.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.markpost.demo.weather.entity.SavedLocation;
import nl.markpost.demo.weather.mapper.SavedLocationMapper;
import nl.markpost.demo.weather.repository.SavedLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavedLocationServiceTest {

  @Mock
  private SavedLocationRepository savedLocationRepository;

  @Mock
  private SavedLocationMapper savedLocationMapper;

  @InjectMocks
  private SavedLocationService savedLocationService;

  private UUID userId;
  private SavedLocation savedLocation;
  private nl.markpost.demo.weather.api.v1.model.Location locationDto;

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

    locationDto = new nl.markpost.demo.weather.api.v1.model.Location();
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
    when(savedLocationRepository.findByUserId(userId)).thenReturn(Arrays.asList(savedLocation));
    when(savedLocationMapper.toApiModel(savedLocation)).thenReturn(locationDto);

    List<nl.markpost.demo.weather.api.v1.model.Location> result = savedLocationService.getSavedLocations(
        userId);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Amsterdam", result.get(0).getName());
    assertEquals(52.3676, result.get(0).getLatitude());
    verify(savedLocationRepository, times(1)).findByUserId(userId);
    verify(savedLocationMapper, times(1)).toApiModel(savedLocation);
  }

  @Test
  void testSaveLocation_NewLocation() {
    when(savedLocationRepository.findByUserIdAndLocationId(userId, 123L)).thenReturn(
        Optional.empty());
    when(savedLocationMapper.toEntity(locationDto)).thenReturn(savedLocation);
    when(savedLocationRepository.save(any(SavedLocation.class))).thenReturn(savedLocation);

    nl.markpost.demo.weather.api.v1.model.Location result = savedLocationService.saveLocation(
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

    nl.markpost.demo.weather.api.v1.model.Location result = savedLocationService.saveLocation(
        userId, locationDto);

    assertNotNull(result);
    assertEquals("Amsterdam", result.getName());
    verify(savedLocationMapper, never()).toEntity(any());
    verify(savedLocationRepository, never()).save(any(SavedLocation.class));
  }

  @Test
  void testDeleteSavedLocation() {
    savedLocationService.deleteSavedLocation(1L, userId);

    verify(savedLocationRepository, times(1)).deleteByIdAndUserId(1L, userId);
  }
}
