package nl.markpost.authentication.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.api.v1.model.PasswordRequest;
import nl.markpost.authentication.api.v1.model.UpdateUserNameRequest;
import nl.markpost.authentication.api.v1.model.UserDetails;
import nl.markpost.authentication.constant.Messages;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.security.JwtKeyProvider;
import nl.markpost.authentication.service.LoginService;
import nl.markpost.authentication.service.UserService;
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

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "ut")
@Import({JwtKeyProvider.class})
class UserControllerTest {

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private LoginService loginService;

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private User mockUser() {
    User user = new User();
    user.setEmail("user@example.com");
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
  @DisplayName("Should delete account successfully")
  void deleteAccount_success() throws Exception {
    User user = mockUser();
    mockSecurityContext(user);
    PasswordRequest passwordRequest = new PasswordRequest();
    passwordRequest.setPassword("password");
    doNothing().when(userService).deleteAccount(user, "password");
    doNothing().when(loginService).logout();
    Message message = MessageResponseUtil.createMessageResponse(Messages.ACCOUNT_DELETED);
    mockMvc.perform(delete("/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(passwordRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));
  }

  @Test
  @DisplayName("Should get user details successfully")
  void getUserDetails_success() throws Exception {
    User user = mockUser();
    mockSecurityContext(user);
    UserDetails userDetails = new UserDetails();
    userDetails.setUserName("user1");
    userDetails.setEmail("user@example.com");
    when(userService.getUserDetails(user)).thenReturn(userDetails);
    mockMvc.perform(get("/v1/user"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userName").value("user1"))
        .andExpect(jsonPath("$.email").value("user@example.com"));
  }

  @Test
  @DisplayName("Should update user name successfully")
  void updateUserName_success() throws Exception {
    User user = mockUser();
    mockSecurityContext(user);
    UpdateUserNameRequest request = new UpdateUserNameRequest();
    request.setUsername("newname");
    doNothing().when(userService).updateUserName(user, "newname");
    mockMvc.perform(put("/v1/user")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }
}