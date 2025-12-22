package ecom.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractImage {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, columnDefinition = "UUID")
  private UUID id;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String imageUrl;

  @Column(nullable = false)
  private String cloudinaryImageId;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;
}
