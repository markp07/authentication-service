package nl.markpost.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import nl.markpost.authentication.api.v1.model.AdminUserDetails;
import nl.markpost.authentication.api.v1.model.AdminUserSummary;
import nl.markpost.authentication.exception.BadRequestException;
import nl.markpost.authentication.model.PasskeyCredential;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordService passwordService;

  @InjectMocks
  private AdminService adminService;

  private User buildUser(UUID id) {
    return User.builder()
        .id(id)
        .userName("user1")
        .email("user1@example.com")
        .password("encoded")
        .is2faEnabled(false)
        .emailVerified(true)
        .blocked(false)
        .roles(new HashSet<>(Set.of("USER")))
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .passkeyCredentials(Collections.emptyList())
        .build();
  }

  @Test
  void getAllUsers_returnsSummaryList() {
    UUID id = UUID.randomUUID();
    when(userRepository.findAll()).thenReturn(List.of(buildUser(id)));

    List<AdminUserSummary> result = adminService.getAllUsers();

    assertEquals(1, result.size());
    assertEquals("user1", result.get(0).getUserName());
    assertFalse(result.get(0).getBlocked());
  }

  @Test
  void getAllUsers_passkeyEnabled_whenCredentialsPresent() {
    UUID id = UUID.randomUUID();
    User user = buildUser(id);
    PasskeyCredential cred = new PasskeyCredential();
    user.setPasskeyCredentials(new ArrayList<>(List.of(cred)));
    when(userRepository.findAll()).thenReturn(List.of(user));

    List<AdminUserSummary> result = adminService.getAllUsers();

    assertTrue(result.get(0).getPasskeyEnabled());
  }

  @Test
  void getAdminUserDetails_returnsFullDetails() {
    UUID id = UUID.randomUUID();
    User user = buildUser(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));

    AdminUserDetails details = adminService.getAdminUserDetails(id);

    assertEquals("user1", details.getUserName());
    assertEquals("user1@example.com", details.getEmail());
    assertEquals(0, details.getFailedLoginAttempts());
    assertFalse(details.getBlocked());
    assertTrue(details.getRoles().contains("USER"));
  }

  @Test
  void getAdminUserDetails_notFound_throws() {
    UUID id = UUID.randomUUID();
    when(userRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(BadRequestException.class, () -> adminService.getAdminUserDetails(id));
  }

  @Test
  void blockUser_setsBlockedTrue() {
    UUID id = UUID.randomUUID();
    User user = buildUser(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));
    when(userRepository.save(any())).thenReturn(user);

    adminService.blockUser(id);

    assertTrue(user.getBlocked());
    verify(userRepository).save(user);
  }

  @Test
  void unblockUser_setsBlockedFalse() {
    UUID id = UUID.randomUUID();
    User user = buildUser(id);
    user.setBlocked(true);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));
    when(userRepository.save(any())).thenReturn(user);

    adminService.unblockUser(id);

    assertFalse(user.getBlocked());
    verify(userRepository).save(user);
  }

  @Test
  void sendResetLink_callsForgotPassword() {
    UUID id = UUID.randomUUID();
    User user = buildUser(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));

    adminService.sendResetLink(id);

    verify(passwordService).forgotPassword("user1@example.com");
  }

  @Test
  void addRole_addsRoleToUser() {
    UUID id = UUID.randomUUID();
    User user = buildUser(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));
    when(userRepository.save(any())).thenReturn(user);

    adminService.addRole(id, "admin");

    assertTrue(user.getRoles().contains("ADMIN"));
    verify(userRepository).save(user);
  }

  @Test
  void addRole_nullRoles_initializesSet() {
    UUID id = UUID.randomUUID();
    User user = buildUser(id);
    user.setRoles(null);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));
    when(userRepository.save(any())).thenReturn(user);

    adminService.addRole(id, "ADMIN");

    assertNotNull(user.getRoles());
    assertTrue(user.getRoles().contains("ADMIN"));
  }

  @Test
  void removeRole_removesRoleFromUser() {
    UUID id = UUID.randomUUID();
    User user = buildUser(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));
    when(userRepository.save(any())).thenReturn(user);

    adminService.removeRole(id, "USER");

    assertFalse(user.getRoles().contains("USER"));
    verify(userRepository).save(user);
  }

  @Test
  void removeRole_roleNotPresent_throws() {
    UUID id = UUID.randomUUID();
    User user = buildUser(id);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));

    assertThrows(BadRequestException.class, () -> adminService.removeRole(id, "ADMIN"));
  }

  @Test
  void removeRole_nullRoles_throws() {
    UUID id = UUID.randomUUID();
    User user = buildUser(id);
    user.setRoles(null);
    when(userRepository.findById(id)).thenReturn(Optional.of(user));

    assertThrows(BadRequestException.class, () -> adminService.removeRole(id, "USER"));
  }
}
