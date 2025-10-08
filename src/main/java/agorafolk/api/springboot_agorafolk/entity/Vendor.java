package agorafolk.api.springboot_agorafolk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "vendors")
public class Vendor {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, columnDefinition = "UUID")
  private UUID id;

  @Column(nullable = false, length = 100)
  private String name;

  @CreationTimestamp
  @Column(updatable = false, nullable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column
  private Instant updatedAt;

  @Builder.Default
  @OneToMany(mappedBy = "vendor", fetch = FetchType.LAZY)
  private List<Product> products = new ArrayList<>();
}
