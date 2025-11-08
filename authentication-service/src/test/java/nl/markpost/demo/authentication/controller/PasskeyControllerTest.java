package nl.markpost.demo.authentication.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import nl.markpost.demo.authentication.api.v1.model.PasskeyInfoDto;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.security.JwtKeyProvider;
import nl.markpost.demo.authentication.service.PasskeyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PasskeyController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "ut")
@Import({JwtKeyProvider.class})
class PasskeyControllerTest {

  @MockitoBean
  private PasskeyService passkeyService;

  @Autowired
  private MockMvc mockMvc;

  private User mockUser() {
    User user = new User();
    user.setEmail("user@example.com");
    user.setId(UUID.randomUUID());
    return user;
  }

  private void mockSecurityContext(User user) {
    Authentication authentication = Mockito.mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(user);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @DisplayName("Should list passkeys")
  void listPasskeys() throws Exception {
    User user = mockUser();
    mockSecurityContext(user);
    
    PasskeyInfoDto dto1 = new PasskeyInfoDto("cred1", "Passkey 1", LocalDateTime.now());
    PasskeyInfoDto dto2 = new PasskeyInfoDto("cred2", "Passkey 2", LocalDateTime.now());
    
    when(passkeyService.listPasskeys(user))
        .thenReturn(Arrays.asList(dto1, dto2));

    mockMvc.perform(get("/v1/passkey"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Should delete passkey")
  void deletePasskey() throws Exception {
    User user = mockUser();
    mockSecurityContext(user);
    
    doNothing().when(passkeyService).deletePasskey(user, "cred1");

    mockMvc.perform(delete("/v1/passkey/cred1"))
        .andExpect(status().isNoContent());
    
    verify(passkeyService).deletePasskey(user, "cred1");
  }
}
