package nl.markpost.demo.authentication.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.api.v1.model.PasskeyInfoDto;
import nl.markpost.demo.authentication.api.v1.model.PasskeyLoginFinishRequest;
import nl.markpost.demo.authentication.api.v1.model.PasskeyLoginStartRequest;
import nl.markpost.demo.authentication.api.v1.model.PublicKeyCredentialAssertionDto;
import nl.markpost.demo.authentication.api.v1.model.PublicKeyCredentialCreationOptionsDto;
import nl.markpost.demo.authentication.api.v1.model.PublicKeyCredentialRequestOptionsDto;
import nl.markpost.demo.authentication.constant.Messages;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.security.JwtKeyProvider;
import nl.markpost.demo.authentication.service.PasskeyService;
import nl.markpost.demo.authentication.util.MessageResponseUtil;
import nl.markpost.demo.authentication.util.ObjectMapperUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

  private final ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();

  private User mockUser() {
    User user = new User();
    user.setEmail("user@example.com");
    user.setUserName("testuser");
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
  @DisplayName("Should list passkeys successfully")
  void listPasskeys_success() throws Exception {
    User user = mockUser();
    mockSecurityContext(user);
    PasskeyInfoDto passkeyInfo = new PasskeyInfoDto();
    passkeyInfo.setCredentialId("credId123");
    passkeyInfo.setName("My Passkey");
    when(passkeyService.listPasskeys(user)).thenReturn(List.of(passkeyInfo));

    mockMvc.perform(get("/v1/passkey"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].credentialId").value("credId123"))
        .andExpect(jsonPath("$[0].name").value("My Passkey"));
  }

  @Test
  @DisplayName("Should start passkey registration successfully")
  void startPasskeyRegistration_success() throws Exception {
    User user = mockUser();
    mockSecurityContext(user);
    PublicKeyCredentialCreationOptionsDto options = new PublicKeyCredentialCreationOptionsDto();
    options.setChallenge("testChallenge");
    when(passkeyService.startRegistration()).thenReturn(options);

    mockMvc.perform(post("/v1/passkey/register/start"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.challenge").value("testChallenge"));
  }

  @Test
  @DisplayName("Should finish passkey registration successfully")
  void finishPasskeyRegistration_success() throws Exception {
    User user = mockUser();
    mockSecurityContext(user);
    doNothing().when(passkeyService).finishRegistration(any(), eq("My Passkey"));

    mockMvc.perform(post("/v1/passkey/register/finish")
            .param("name", "My Passkey")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Should start passkey login successfully")
  void startPasskeyLogin_success() throws Exception {
    PasskeyLoginStartRequest request = new PasskeyLoginStartRequest();
    request.setEmail("user@example.com");
    PublicKeyCredentialRequestOptionsDto options = new PublicKeyCredentialRequestOptionsDto();
    options.setChallenge("loginChallenge");
    when(passkeyService.startAuthentication("user@example.com")).thenReturn(options);

    mockMvc.perform(post("/v1/passkey/login/start")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.challenge").value("loginChallenge"));
  }

  @Test
  @DisplayName("Should finish passkey login successfully")
  void finishPasskeyLogin_success() throws Exception {
    PasskeyLoginFinishRequest request = new PasskeyLoginFinishRequest();
    request.setEmail("user@example.com");
    request.setCredential(new PublicKeyCredentialAssertionDto());
    Message message = MessageResponseUtil.createMessageResponse(Messages.LOGIN_SUCCESS);
    when(passkeyService.finishAuthentication(eq("user@example.com"), any()))
        .thenReturn(ResponseEntity.ok(message));

    mockMvc.perform(post("/v1/passkey/login/finish")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()));
  }

  @Test
  @DisplayName("Should start usernameless passkey login successfully")
  void startUsernamelessPasskeyLogin_success() throws Exception {
    PublicKeyCredentialRequestOptionsDto options = new PublicKeyCredentialRequestOptionsDto();
    options.setChallenge("usernamelessChallenge");
    when(passkeyService.startUsernamelessAuthentication()).thenReturn(options);

    mockMvc.perform(post("/v1/passkey/login/usernameless/start"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.challenge").value("usernamelessChallenge"));
  }

  @Test
  @DisplayName("Should finish usernameless passkey login successfully")
  void finishUsernamelessPasskeyLogin_success() throws Exception {
    PublicKeyCredentialAssertionDto credential = new PublicKeyCredentialAssertionDto();
    credential.setId("credentialId");
    Message message = MessageResponseUtil.createMessageResponse(Messages.LOGIN_SUCCESS);
    when(passkeyService.finishUsernamelessAuthentication(any())).thenReturn(ResponseEntity.ok(message));

    mockMvc.perform(post("/v1/passkey/login/usernameless/finish")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(credential)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()));
  }

  @Test
  @DisplayName("Should delete passkey successfully")
  void deletePasskey_success() throws Exception {
    User user = mockUser();
    mockSecurityContext(user);
    doNothing().when(passkeyService).deletePasskey("credentialId123");

    mockMvc.perform(delete("/v1/passkey/credentialId123"))
        .andExpect(status().isNoContent());
  }
}
