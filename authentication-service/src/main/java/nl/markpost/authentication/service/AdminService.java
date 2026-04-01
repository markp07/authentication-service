package nl.markpost.authentication.service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.api.v1.model.AdminUserDetails;
import nl.markpost.authentication.api.v1.model.AdminUserSummary;
import nl.markpost.authentication.exception.BadRequestException;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for admin user management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

  private final UserRepository userRepository;
  private final PasswordService passwordService;

  /**
   * Returns a summary list of all users.
   */
  @Transactional(readOnly = true)
  public List<AdminUserSummary> getAllUsers() {
    return userRepository.findAll().stream()
        .map(this::toSummary)
        .toList();
  }

  /**
   * Returns the full admin details for a single user.
   */
  @Transactional(readOnly = true)
  public AdminUserDetails getAdminUserDetails(UUID id) {
    User user = findUser(id);
    boolean passkeyEnabled = user.getPasskeyCredentials() != null
        && !user.getPasskeyCredentials().isEmpty();

    return AdminUserDetails.builder()
        .id(user.getId())
        .userName(user.getUsername())
        .email(user.getEmail())
        .createdAt(toOffset(user.getCreatedAt()))
        .updatedAt(toOffset(user.getUpdatedAt()))
        .emailVerified(user.isEmailVerified())
        .twoFactorEnabled(user.is2faEnabled())
        .passkeyEnabled(passkeyEnabled)
        .blocked(Boolean.TRUE.equals(user.getBlocked()))
        .roles(user.getRoles() != null ? new ArrayList<>(user.getRoles()) : new ArrayList<>())
        .failedLoginAttempts(user.getFailedLoginAttempts())
        .accountLockedUntil(toOffset(user.getAccountLockedUntil()))
        .build();
  }

  /**
   * Blocks the user account.
   */
  @Transactional
  public void blockUser(UUID id) {
    User user = findUser(id);
    user.setBlocked(true);
    userRepository.save(user);
    log.info("Admin blocked user: {}", user.getEmail());
  }

  /**
   * Unblocks the user account.
   */
  @Transactional
  public void unblockUser(UUID id) {
    User user = findUser(id);
    user.setBlocked(false);
    userRepository.save(user);
    log.info("Admin unblocked user: {}", user.getEmail());
  }

  /**
   * Sends a password reset link to the user via the existing forgot-password flow.
   */
  @Transactional
  public void sendResetLink(UUID id) {
    User user = findUser(id);
    passwordService.forgotPassword(user.getEmail());
    log.info("Admin triggered password reset for user: {}", user.getEmail());
  }

  /**
   * Adds a role to the user.
   */
  @Transactional
  public void addRole(UUID id, String role) {
    User user = findUser(id);
    if (user.getRoles() == null) {
      user.setRoles(new HashSet<>());
    }
    user.getRoles().add(role.toUpperCase());
    userRepository.save(user);
    log.info("Admin added role {} to user: {}", role, user.getEmail());
  }

  /**
   * Removes a role from the user.
   */
  @Transactional
  public void removeRole(UUID id, String role) {
    User user = findUser(id);
    if (user.getRoles() == null || !user.getRoles().remove(role.toUpperCase())) {
      throw new BadRequestException("Role not found on user");
    }
    userRepository.save(user);
    log.info("Admin removed role {} from user: {}", role, user.getEmail());
  }

  private User findUser(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new BadRequestException("User not found"));
  }

  private AdminUserSummary toSummary(User user) {
    boolean passkeyEnabled = user.getPasskeyCredentials() != null
        && !user.getPasskeyCredentials().isEmpty();
    return AdminUserSummary.builder()
        .id(user.getId())
        .userName(user.getUsername())
        .email(user.getEmail())
        .createdAt(toOffset(user.getCreatedAt()))
        .emailVerified(user.isEmailVerified())
        .twoFactorEnabled(user.is2faEnabled())
        .passkeyEnabled(passkeyEnabled)
        .blocked(Boolean.TRUE.equals(user.getBlocked()))
        .roles(user.getRoles() != null ? new ArrayList<>(user.getRoles()) : new ArrayList<>())
        .build();
  }

  private java.time.OffsetDateTime toOffset(java.time.LocalDateTime ldt) {
    return ldt != null ? ldt.atZone(ZoneId.systemDefault()).toOffsetDateTime() : null;
  }
}
