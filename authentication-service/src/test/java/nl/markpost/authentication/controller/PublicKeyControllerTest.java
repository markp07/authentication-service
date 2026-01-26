package nl.markpost.authentication.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nl.markpost.authentication.security.JwtKeyProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PublicKeyController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "ut")
class PublicKeyControllerTest {

  @MockitoBean
  private JwtKeyProvider keyProvider;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("Should return public key PEM string successfully")
  void getPublicKey_success() throws Exception {
    String pem = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn...\n-----END PUBLIC KEY-----";
    when(keyProvider.getPublicKeyAsPem()).thenReturn(pem);
    mockMvc.perform(get("/v1/public-key"))
        .andExpect(status().isOk())
        .andExpect(content().string(pem));
  }
}