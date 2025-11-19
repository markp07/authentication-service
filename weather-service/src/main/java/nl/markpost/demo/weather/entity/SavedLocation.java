package nl.markpost.demo.weather.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Entity representing a saved location for a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "saved_locations")
public class SavedLocation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  @NotNull
  private UUID userId;

  @Column(name = "location_id", nullable = false)
  @NotNull
  private Long locationId;

  @Column(nullable = false)
  @NotBlank
  private String name;

  @Column(nullable = false)
  @NotNull
  private Double latitude;

  @Column(nullable = false)
  @NotNull
  private Double longitude;

  @Column
  private String country;

  @Column(name = "country_code")
  private String countryCode;

  @Column
  private String admin1;

  @Column
  private String timezone;

  @Column(name = "display_order")
  private Integer displayOrder;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
