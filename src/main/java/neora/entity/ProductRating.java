package neora.entity;

import neora.model.Rating;
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
@Table(name = "products_ratings")
public class ProductRating {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false, columnDefinition = "UUID")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "rating", nullable = false)
  private Integer ratingValue;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  public void setRating(Rating rating) {
    this.ratingValue = rating.getRating();
  }

  public Rating getRatingEnum() {
    return Rating.fromValue(this.ratingValue);
  }
}
