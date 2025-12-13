package nl.markpost.authentication.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.constant.Messages;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.security.JwtKeyProvider;
import nl.markpost.authentication.service.EmailVerificationService;
import nl.markpost.authentication.util.MessageResponseUtil;
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

@WebMvcTest(controllers = EmailController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "ut")
@Import({JwtKeyProvider.class})
class EmailControllerTest {

  @MockitoBean
  private EmailVerificationService emailVerificationService;

  @Autowired
  private MockMvc mockMvc;

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
  @DisplayName("Should verify email successfully")
  void verifyEmail_success() throws Exception {
    String token = "valid-verification-token";
    doNothing().when(emailVerificationService).verifyEmail(token);
    Message message = MessageResponseUtil.createMessageResponse(Messages.EMAIL_VERIFIED);

    mockMvc.perform(get("/v1/email/verify")
            .param("token", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));

    verify(emailVerificationService).verifyEmail(token);
  }

  @Test
  @DisplayName("Should resend verification email successfully")
  void resendVerificationEmail_success() throws Exception {
    User user = mockUser();
    mockSecurityContext(user);
    doNothing().when(emailVerificationService).sendVerificationEmail(user);
    Message message = MessageResponseUtil.createMessageResponse(Messages.VERIFICATION_EMAIL_SENT);

    mockMvc.perform(post("/v1/email/resend-verification"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(message.getCode()))
        .andExpect(jsonPath("$.description").value(message.getDescription()));

    verify(emailVerificationService).sendVerificationEmail(user);
  }
}
