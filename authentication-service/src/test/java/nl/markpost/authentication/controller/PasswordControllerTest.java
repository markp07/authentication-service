package nl.markpost.authentication.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.markpost.authentication.api.v1.model.ChangePasswordRequest;
import nl.markpost.authentication.api.v1.model.ForgotPasswordRequest;
import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.api.v1.model.ResetPasswordRequest;
import nl.markpost.authentication.constant.Messages;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.security.JwtKeyProvider;
import nl.markpost.authentication.service.PasswordService;
import nl.markpost.authentication.util.MessageResponseUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PasswordController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "ut")
@Import({JwtKeyProvider.class})
class PasswordControllerTest {

  @MockitoBean
  private PasswordService passwordService;

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("Should change password successfully")
  void changePassword_success() throws Exception {
    ChangePasswordRequest request = new ChangePasswordRequest();
    request.setOldPassword("oldpass");
    request.setNewPassword("newpass");
    User user = new User();
    user.setEmail("user@example.com");
    Authentication authentication = Mockito.mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(user);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
    doNothing().when(passwordService).changePassword(user, request);
    Message message = MessageResponseUtil.createMessageResponse(Messages.PASSWORD_CHANGE_SUCCESS);
    mockMvc.perform(put("/v1/password/change")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }

  @Test
  @DisplayName("Should handle forgot password request successfully")
  void forgotPassword_success() throws Exception {
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail("user@example.com");
    doNothing().when(passwordService).forgotPassword("user@example.com");
    Message message = MessageResponseUtil.createMessageResponse(Messages.RESET_SENT_SUCCESS);
    mockMvc.perform(post("/v1/password/forgot")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }

  @Test
  @DisplayName("Should reset password successfully")
  void resetPassword_success() throws Exception {
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setResetToken("token123");
    request.setNewPassword("newpass");
    doNothing().when(passwordService).resetPassword("token123", "newpass");
    Message message = MessageResponseUtil.createMessageResponse(Messages.RESET_SUCCESS);
    mockMvc.perform(post("/v1/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }

  @Test
  @DisplayName("Should return 400 Bad Request for missing email in forgot password")
  void forgotPassword_missingEmail() throws Exception {
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    mockMvc.perform(post("/v1/password/forgot")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should return 400 Bad Request for missing reset token in reset password")
  void resetPassword_missingToken() throws Exception {
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setNewPassword("newpass");
    mockMvc.perform(post("/v1/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}