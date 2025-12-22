package ecom.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "vendors",
    uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
public class Vendor {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, columnDefinition = "UUID")
  private UUID id;

  @Column(unique = true, nullable = false, length = 100)
  private String name;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  private Instant createdAt;

  @UpdateTimestamp @Column private Instant updatedAt;

  @Builder.Default
  @OneToMany(mappedBy = "vendor", fetch = FetchType.LAZY)
  private List<Product> products = new ArrayList<>();

  @OneToOne(mappedBy = "vendor", cascade = CascadeType.ALL, orphanRemoval = true)
    private VendorImage vendorImage;
}
