package nl.markpost.demo.authentication.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.markpost.demo.authentication.api.v1.model.LoginRequest;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.api.v1.model.RegisterRequest;
import nl.markpost.demo.authentication.constant.Messages;
import nl.markpost.demo.authentication.security.JwtKeyProvider;
import nl.markpost.demo.authentication.service.LoginService;
import nl.markpost.demo.authentication.util.MessageResponseUtil;
import nl.markpost.demo.authentication.util.ObjectMapperUtil;
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

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "ut")
@Import({JwtKeyProvider.class})
class AuthenticationControllerTest {

  @MockitoBean
  private LoginService loginService;

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = ObjectMapperUtil.createObjectMapper();

  @Test
  @DisplayName("Should login successfully with valid credentials")
  void login_success() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("user@example.com");
    loginRequest.setPassword("password");
    Message message = MessageResponseUtil.createMessageResponse(Messages.LOGIN_SUCCESS);
    when(loginService.login(loginRequest)).thenReturn(
        org.springframework.http.ResponseEntity.ok(message));

    mockMvc.perform(post("/v1/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }

  @Test
  @DisplayName("Should logout successfully")
  void logout_success() throws Exception {
    doNothing().when(loginService).logout();
    Message message = MessageResponseUtil.createMessageResponse(Messages.LOGOUT_SUCCESS);
    mockMvc.perform(post("/v1/logout"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }

  @Test
  @DisplayName("Should refresh token successfully")
  void refresh_success() throws Exception {
    doNothing().when(loginService).refresh();
    Message message = MessageResponseUtil.createMessageResponse(Messages.REFRESH_SUCCESS);
    mockMvc.perform(post("/v1/refresh"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }

  @Test
  @DisplayName("Should register successfully with valid data")
  void register_success() throws Exception {
    RegisterRequest registerRequest = new RegisterRequest();
    registerRequest.setUserName("newuser");
    registerRequest.setEmail("newuser@example.com");
    registerRequest.setPassword("password");
    doNothing().when(loginService).register(registerRequest);
    Message message = MessageResponseUtil.createMessageResponse(Messages.REGISTRATION_SUCCESS);
    mockMvc.perform(post("/v1/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }

  @Test
  @DisplayName("Should return 400 Bad Request for missing login email")
  void login_missingEmail() throws Exception {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setPassword("password");
    mockMvc.perform(post("/v1/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Should return 400 Bad Request for missing register email")
  void register_missingEmail() throws Exception {
    RegisterRequest registerRequest = new RegisterRequest();
    registerRequest.setPassword("password");
    mockMvc.perform(post("/v1/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isBadRequest());
  }
}