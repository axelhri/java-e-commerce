package neora.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import neora.model.StockReason;
import neora.model.StockType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stock_movements")
public class StockMovement {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, columnDefinition = "UUID")
  private UUID id;

  @Column(nullable = false)
  @PositiveOrZero
  private Integer quantity;

  @Builder.Default
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StockType type = StockType.IN;

  @Builder.Default
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StockReason reason = StockReason.NEW;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp @Column private Instant updatedAt;
}
