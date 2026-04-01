package nl.markpost.authentication.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.api.v1.controller.AdminApi;
import nl.markpost.authentication.api.v1.model.AddRoleRequest;
import nl.markpost.authentication.api.v1.model.AdminUserDetails;
import nl.markpost.authentication.api.v1.model.AdminUserSummary;
import nl.markpost.authentication.api.v1.model.Message;
import nl.markpost.authentication.constant.Messages;
import nl.markpost.authentication.service.AdminService;
import nl.markpost.authentication.util.MessageResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for admin user management endpoints. Access is restricted to users with the ADMIN
 * role via SecurityConfig.
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Slf4j
public class AdminController implements AdminApi {

  private final AdminService adminService;

  @Override
  public ResponseEntity<List<AdminUserSummary>> getAllUsers() {
    return ResponseEntity.ok(adminService.getAllUsers());
  }

  @Override
  public ResponseEntity<AdminUserDetails> getAdminUserDetails(UUID id) {
    return ResponseEntity.ok(adminService.getAdminUserDetails(id));
  }

  @Override
  public ResponseEntity<Message> blockUser(UUID id) {
    adminService.blockUser(id);
    return ResponseEntity.ok(MessageResponseUtil.createMessageResponse(Messages.ADMIN_BLOCK_SUCCESS));
  }

  @Override
  public ResponseEntity<Message> unblockUser(UUID id) {
    adminService.unblockUser(id);
    return ResponseEntity.ok(
        MessageResponseUtil.createMessageResponse(Messages.ADMIN_UNBLOCK_SUCCESS));
  }

  @Override
  public ResponseEntity<Message> sendAdminResetLink(UUID id) {
    adminService.sendResetLink(id);
    return ResponseEntity.ok(
        MessageResponseUtil.createMessageResponse(Messages.ADMIN_RESET_LINK_SENT));
  }

  @Override
  public ResponseEntity<Message> addUserRole(UUID id, AddRoleRequest addRoleRequest) {
    adminService.addRole(id, addRoleRequest.getRole());
    return ResponseEntity.ok(MessageResponseUtil.createMessageResponse(Messages.ADMIN_ROLE_ADDED));
  }

  @Override
  public ResponseEntity<Message> removeUserRole(UUID id, String role) {
    adminService.removeRole(id, role);
    return ResponseEntity.ok(
        MessageResponseUtil.createMessageResponse(Messages.ADMIN_ROLE_REMOVED));
  }
}
