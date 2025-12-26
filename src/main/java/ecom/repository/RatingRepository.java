package ecom.repository;

import ecom.dto.ProductAverageRating;
import ecom.entity.ProductRating;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<ProductRating, UUID> {
  @Query(
      """
    SELECT new ecom.dto.ProductAverageRating(
        pr.product.id,
        AVG(pr.ratingValue)
    )
    FROM ProductRating pr
    WHERE pr.product.id IN :productIds
    GROUP BY pr.product.id
  """)
  List<ProductAverageRating> getAverageRatingForProducts(List<UUID> productIds);
}
