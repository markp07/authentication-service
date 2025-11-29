package nl.markpost.demo.weather.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import nl.markpost.demo.weather.api.v1.model.Location;
import nl.markpost.demo.weather.api.v1.model.ReorderRequest;
import nl.markpost.demo.weather.service.LocationsService;
import nl.markpost.demo.weather.util.ObjectMapperUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(controllers = LocationsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "ut")
class LocationsControllerTest {

  @MockitoBean
  private LocationsService locationsService;

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();

  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
  }

  private RequestPostProcessor addJwtClaims() {
    return request -> {
      Map<String, Object> claimsMap = new HashMap<>();
      claimsMap.put("userId", userId.toString());
      claimsMap.put("sub", "test@example.com");
      Claims claims = new DefaultClaims(claimsMap);
      request.setAttribute("jwtClaims", claims);
      return request;
    };
  }

  @Test
  @DisplayName("Should search locations successfully")
  void searchLocations_success() throws Exception {
    Location location = new Location();
    location.setId(1L);
    location.setName("Amsterdam");
    location.setCountry("Netherlands");
    when(locationsService.searchLocations("Amsterdam")).thenReturn(List.of(location));

    mockMvc.perform(get("/v1/search")
            .param("name", "Amsterdam"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Amsterdam"))
        .andExpect(jsonPath("$[0].country").value("Netherlands"));
  }

  @Test
  @DisplayName("Should return empty list when no locations found")
  void searchLocations_noResults() throws Exception {
    when(locationsService.searchLocations("nonexistent")).thenReturn(List.of());

    mockMvc.perform(get("/v1/search")
            .param("name", "nonexistent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  @DisplayName("Should get saved locations successfully")
  void getSavedLocations_success() throws Exception {
    Location location = new Location();
    location.setId(1L);
    location.setName("Amsterdam");
    when(locationsService.getSavedLocations(userId)).thenReturn(List.of(location));

    mockMvc.perform(get("/v1/saved-locations")
            .with(addJwtClaims()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Amsterdam"));
  }

  @Test
  @DisplayName("Should save location successfully")
  void saveLocation_success() throws Exception {
    Location location = new Location();
    location.setName("Amsterdam");
    location.setLatitude(52.37);
    location.setLongitude(4.89);

    Location savedLocation = new Location();
    savedLocation.setId(1L);
    savedLocation.setName("Amsterdam");
    savedLocation.setLatitude(52.37);
    savedLocation.setLongitude(4.89);

    when(locationsService.saveLocation(eq(userId), any(Location.class))).thenReturn(savedLocation);

    mockMvc.perform(post("/v1/saved-locations")
            .with(addJwtClaims())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(location)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Amsterdam"));
  }

  @Test
  @DisplayName("Should delete saved location successfully")
  void deleteSavedLocation_success() throws Exception {
    doNothing().when(locationsService).deleteSavedLocation(1L, userId);

    mockMvc.perform(delete("/v1/saved-locations/1")
            .with(addJwtClaims()))
        .andExpect(status().isNoContent());

    verify(locationsService).deleteSavedLocation(1L, userId);
  }

  @Test
  @DisplayName("Should reorder saved locations successfully")
  void reorderSavedLocations_success() throws Exception {
    ReorderRequest reorderRequest = new ReorderRequest();
    reorderRequest.setLocationIds(List.of(3L, 1L, 2L));

    doNothing().when(locationsService).reorderLocations(eq(userId), eq(List.of(3L, 1L, 2L)));

    mockMvc.perform(put("/v1/saved-locations/reorder")
            .with(addJwtClaims())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(reorderRequest)))
        .andExpect(status().isNoContent());

    verify(locationsService).reorderLocations(userId, List.of(3L, 1L, 2L));
  }
}
