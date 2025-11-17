package nl.markpost.demo.weather.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import nl.markpost.demo.weather.mapper.WeatherModelMapper;
import nl.markpost.demo.weather.service.SavedLocationService;
import nl.markpost.demo.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WeatherController.class)
@ActiveProfiles("ut")
class SavedLocationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private WeatherService weatherService;

  @MockBean
  private WeatherModelMapper weatherModelMapper;

  @MockBean
  private SavedLocationService savedLocationService;

  @MockBean
  private HttpServletRequest request;

  private UUID userId;
  private nl.markpost.demo.weather.api.v1.model.Location locationDto;
  private Claims claims;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    Map<String, Object> claimsMap = new HashMap<>();
    claimsMap.put("userId", userId.toString());
    claims = Jwts.claims().add(claimsMap).build();

    locationDto = new nl.markpost.demo.weather.api.v1.model.Location();
    locationDto.setId(123L);
    locationDto.setName("Amsterdam");
    locationDto.setLatitude(52.3676);
    locationDto.setLongitude(4.9041);
    locationDto.setCountry("Netherlands");
    locationDto.setCountryCode("NL");
    locationDto.setAdmin1("North Holland");
    locationDto.setTimezone("Europe/Amsterdam");

    when(request.getAttribute("jwtClaims")).thenReturn(claims);
  }

  @Test
  void testGetSavedLocations() throws Exception {
    when(savedLocationService.getSavedLocations(any(UUID.class))).thenReturn(
        Arrays.asList(locationDto));

    mockMvc.perform(get("/v1/saved-locations"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].name").value("Amsterdam"));
  }

  @Test
  void testSaveLocation() throws Exception {
    when(savedLocationService.saveLocation(any(UUID.class),
        any(nl.markpost.demo.weather.api.v1.model.Location.class))).thenReturn(locationDto);

    mockMvc.perform(post("/v1/saved-locations")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(locationDto)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name").value("Amsterdam"));
  }

  @Test
  void testDeleteSavedLocation() throws Exception {
    mockMvc.perform(delete("/v1/saved-locations/1"))
        .andExpect(status().isNoContent());
  }
}
