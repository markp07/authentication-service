package nl.markpost.authentication.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password", "resetToken", "totpSecret"})
@Entity
@Table(name = "users")
@Builder(toBuilder = true)
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true, nullable = false)
  @NotBlank
  private String email;

  @Column(unique = true, nullable = false)
  @NotBlank
  private String userName;

  @JsonIgnore
  @NotBlank
  private String password;

  @ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @jakarta.persistence.JoinColumn(name = "user_id"))
  @Column(name = "role", nullable = false)
  private Set<String> roles;

  @Column(name = "reset_token")
  private String resetToken;

  @Column(name = "reset_token_created_at")
  private LocalDateTime resetTokenCreatedAt;

  @Column(name = "totp_secret")
  private String totpSecret;

  @Column(name = "is_2fa_enabled")
  private Boolean is2faEnabled;

  @Column(name = "totp_setup_created_at")
  private LocalDateTime totpSetupCreatedAt;

  @Column(name = "backup_code")
  private String backupCode;

  @Column(name = "email_verified")
  private Boolean emailVerified;

  @Column(name = "email_verification_token")
  private String emailVerificationToken;

  @Column(name = "email_verification_token_created_at")
  private LocalDateTime emailVerificationTokenCreatedAt;

  @Column(name = "last_verification_email_sent_at")
  private LocalDateTime lastVerificationEmailSentAt;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
  private List<PasskeyCredential> passkeyCredentials;

  @Override
  public Set<SimpleGrantedAuthority> getAuthorities() {
    return roles == null ? Set.of() : roles.stream().map(SimpleGrantedAuthority::new)
        .collect(java.util.stream.Collectors.toSet());
  }

  @Override
  public String getUsername() {
    return userName;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  // Custom getters/setters to maintain backward compatibility with existing code
  public boolean is2faEnabled() {
    return Boolean.TRUE.equals(is2faEnabled);
  }

  public void set2faEnabled(boolean enabled) {
    this.is2faEnabled = enabled;
  }

  public boolean isEmailVerified() {
    return Boolean.TRUE.equals(emailVerified);
  }

  public void setEmailVerified(boolean verified) {
    this.emailVerified = verified;
  }
}
