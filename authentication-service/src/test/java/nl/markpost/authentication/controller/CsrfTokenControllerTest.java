package nl.markpost.authentication.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nl.markpost.authentication.api.v1.model.CsrfTokenResponse;
import nl.markpost.authentication.security.JwtKeyProvider;
import nl.markpost.authentication.service.CsrfTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CsrfTokenController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "ut")
@Import({JwtKeyProvider.class})
class CsrfTokenControllerTest {

  @MockitoBean
  private CsrfTokenService csrfTokenService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("Should return CSRF token successfully")
  void getCsrfToken_success() throws Exception {
    String token = "test-csrf-token-value";
    CsrfTokenResponse response = CsrfTokenResponse.builder().token(token).build();
    when(csrfTokenService.generateCsrfToken()).thenReturn(response);

    mockMvc.perform(get("/v1/csrf"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(token));
  }
}
