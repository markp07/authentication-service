package nl.markpost.authentication.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import nl.markpost.authentication.api.v1.model.AddRoleRequest;
import nl.markpost.authentication.api.v1.model.AdminUserDetails;
import nl.markpost.authentication.api.v1.model.AdminUserSummary;
import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.constant.Messages;
import nl.markpost.authentication.security.JwtKeyProvider;
import nl.markpost.authentication.service.AdminService;
import nl.markpost.authentication.util.MessageResponseUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "ut")
@Import({JwtKeyProvider.class})
class AdminControllerTest {

  @MockitoBean
  private AdminService adminService;

  @Autowired
  private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @DisplayName("Should return list of all users")
  void getAllUsers_success() throws Exception {
    AdminUserSummary summary = AdminUserSummary.builder()
        .id(UUID.randomUUID())
        .userName("user1")
        .email("user1@example.com")
        .emailVerified(true)
        .twoFactorEnabled(false)
        .passkeyEnabled(false)
        .blocked(false)
        .roles(List.of("USER"))
        .build();

    when(adminService.getAllUsers()).thenReturn(List.of(summary));

    mockMvc.perform(get("/v1/admin/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].userName").value("user1"))
        .andExpect(jsonPath("$[0].email").value("user1@example.com"))
        .andExpect(jsonPath("$[0].blocked").value(false));
  }

  @Test
  @DisplayName("Should return admin user details")
  void getAdminUserDetails_success() throws Exception {
    UUID id = UUID.randomUUID();
    AdminUserDetails details = AdminUserDetails.builder()
        .id(id)
        .userName("user1")
        .email("user1@example.com")
        .emailVerified(true)
        .twoFactorEnabled(false)
        .passkeyEnabled(false)
        .blocked(false)
        .roles(List.of("USER"))
        .failedLoginAttempts(0)
        .build();

    when(adminService.getAdminUserDetails(id)).thenReturn(details);

    mockMvc.perform(get("/v1/admin/users/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userName").value("user1"))
        .andExpect(jsonPath("$.email").value("user1@example.com"))
        .andExpect(jsonPath("$.failedLoginAttempts").value(0));
  }

  @Test
  @DisplayName("Should block user successfully")
  void blockUser_success() throws Exception {
    UUID id = UUID.randomUUID();
    doNothing().when(adminService).blockUser(id);
    Message expected = MessageResponseUtil.createMessageResponse(Messages.ADMIN_BLOCK_SUCCESS);

    mockMvc.perform(put("/v1/admin/users/{id}/block", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(expected.getCode()))
        .andExpect(jsonPath("$.description").value(expected.getDescription()));
  }

  @Test
  @DisplayName("Should unblock user successfully")
  void unblockUser_success() throws Exception {
    UUID id = UUID.randomUUID();
    doNothing().when(adminService).unblockUser(id);
    Message expected = MessageResponseUtil.createMessageResponse(Messages.ADMIN_UNBLOCK_SUCCESS);

    mockMvc.perform(put("/v1/admin/users/{id}/unblock", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(expected.getCode()))
        .andExpect(jsonPath("$.description").value(expected.getDescription()));
  }

  @Test
  @DisplayName("Should send admin reset link successfully")
  void sendAdminResetLink_success() throws Exception {
    UUID id = UUID.randomUUID();
    doNothing().when(adminService).sendResetLink(id);
    Message expected = MessageResponseUtil.createMessageResponse(Messages.ADMIN_RESET_LINK_SENT);

    mockMvc.perform(post("/v1/admin/users/{id}/send-reset-link", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(expected.getCode()))
        .andExpect(jsonPath("$.description").value(expected.getDescription()));
  }

  @Test
  @DisplayName("Should add role to user successfully")
  void addUserRole_success() throws Exception {
    UUID id = UUID.randomUUID();
    AddRoleRequest request = new AddRoleRequest();
    request.setRole("ADMIN");
    doNothing().when(adminService).addRole(eq(id), any());
    Message expected = MessageResponseUtil.createMessageResponse(Messages.ADMIN_ROLE_ADDED);

    mockMvc.perform(post("/v1/admin/users/{id}/roles", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(expected.getCode()))
        .andExpect(jsonPath("$.description").value(expected.getDescription()));
  }

  @Test
  @DisplayName("Should remove role from user successfully")
  void removeUserRole_success() throws Exception {
    UUID id = UUID.randomUUID();
    doNothing().when(adminService).removeRole(eq(id), eq("ADMIN"));
    Message expected = MessageResponseUtil.createMessageResponse(Messages.ADMIN_ROLE_REMOVED);

    mockMvc.perform(delete("/v1/admin/users/{id}/roles/{role}", id, "ADMIN"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(expected.getCode()))
        .andExpect(jsonPath("$.description").value(expected.getDescription()));
  }
}
