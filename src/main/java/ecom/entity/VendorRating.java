package ecom.entity;

import ecom.model.Rating;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
    name = "vendors_ratings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"vendor_id", "user_id"}))
public class VendorRating {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, columnDefinition = "UUID")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vendor_id", nullable = false)
  private Vendor vendor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.ORDINAL)
  @Column(nullable = false)
  private Rating rating;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  private Instant createdAt;
}
