package agorafolk.api.springboot_agorafolk.entity;

import agorafolk.api.springboot_agorafolk.model.Rating;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "vendors_ratings")
public class VendorRating {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, columnDefinition = "UUID")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "vendor_id", nullable = false)
  private Vendor vendor;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private Rating rating;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  private Instant createdAt;
}
