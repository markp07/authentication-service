package nl.markpost.authentication.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.markpost.authentication.api.v1.model.BackupCodeResponse;
import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.api.v1.model.PasswordRequest;
import nl.markpost.authentication.api.v1.model.TOTPCode;
import nl.markpost.authentication.api.v1.model.TOTPSetupResponse;
import nl.markpost.authentication.api.v1.model.TOTPVerifyRequest;
import nl.markpost.authentication.constant.Messages;
import nl.markpost.authentication.security.JwtKeyProvider;
import nl.markpost.authentication.service.LoginService;
import nl.markpost.authentication.service.Manage2faService;
import nl.markpost.authentication.util.MessageResponseUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = Manage2faController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "ut")
@Import({JwtKeyProvider.class})
class Manage2faControllerTest {

  @MockitoBean
  private Manage2faService manage2faService;

  @MockitoBean
  private LoginService loginService;

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("Should setup 2FA successfully")
  void setup2fa_success() throws Exception {
    TOTPSetupResponse response = new TOTPSetupResponse();
    when(manage2faService.setup2fa()).thenReturn(response);
    mockMvc.perform(post("/v1/2fa/setup"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Should enable 2FA successfully")
  void enable2fa_success() throws Exception {
    TOTPCode code = new TOTPCode();
    code.setCode("123456");
    doNothing().when(manage2faService).enable2fa(code);
    Message message = MessageResponseUtil.createMessageResponse(Messages.TWO_FA_SETUP_SUCCESS);
    mockMvc.perform(post("/v1/2fa/confirm")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(code)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }

  @Test
  @DisplayName("Should disable 2FA successfully")
  void disable2fa_success() throws Exception {
    PasswordRequest passwordRequest = new PasswordRequest();
    passwordRequest.setPassword("password");
    doNothing().when(manage2faService).disable2fa(passwordRequest);
    doNothing().when(loginService).logout();
    Message message = MessageResponseUtil.createMessageResponse(Messages.TWO_FA_DISABLED);
    mockMvc.perform(post("/v1/2fa/disable")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }

  @Test
  @DisplayName("Should verify 2FA successfully")
  void verify2fa_success() throws Exception {
    TOTPVerifyRequest request = TOTPVerifyRequest.builder().code("123456").build();
    doNothing().when(manage2faService).verify2fa(request);
    Message message = MessageResponseUtil.createMessageResponse(Messages.LOGIN_SUCCESS);
    mockMvc.perform(post("/v1/2fa/verify")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }

  @Test
  @DisplayName("Should generate backup code successfully")
  void generateBackupCode_success() throws Exception {
    BackupCodeResponse response = new BackupCodeResponse();
    response.setBackupCode("backup123");
    when(manage2faService.generateBackupCode()).thenReturn(response);
    mockMvc.perform(post("/v1/2fa/backup-code"))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("Should reset 2FA successfully with valid backup code")
  void reset2fa_success() throws Exception {
    BackupCodeResponse backupCode = new BackupCodeResponse();
    backupCode.setBackupCode("backup123");
    when(manage2faService.reset2faWithBackupCode("backup123")).thenReturn(true);
    doNothing().when(loginService).logout();
    Message message = MessageResponseUtil.createMessageResponse(Messages.TWO_FA_DISABLED);
    mockMvc.perform(post("/v1/2fa/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(backupCode)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }

  @Test
  @DisplayName("Should return 403 Forbidden for invalid backup code")
  void reset2fa_invalidBackupCode() throws Exception {
    BackupCodeResponse backupCode = new BackupCodeResponse();
    backupCode.setBackupCode("invalid");
    when(manage2faService.reset2faWithBackupCode("invalid")).thenReturn(false);
    Message message = MessageResponseUtil.createMessageResponse(
        Messages.TWO_FA_BACKUP_CODE_INVALID);
    mockMvc.perform(post("/v1/2fa/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(backupCode)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }
}