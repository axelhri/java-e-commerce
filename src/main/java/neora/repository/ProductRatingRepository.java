package neora.repository;

import java.util.Optional;
import java.util.UUID;
import neora.entity.Product;
import neora.entity.ProductRating;
import neora.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRatingRepository extends JpaRepository<ProductRating, UUID> {
  Optional<ProductRating> findByUserAndProduct(User user, Product product);

  @Query("SELECT AVG(pr.ratingValue) FROM ProductRating pr WHERE pr.product.vendor.id = :vendorId")
  Double getAverageRatingByVendorId(@Param("vendorId") UUID vendorId);

  Page<ProductRating> findByProductId(UUID productId, Pageable pageable);

  @Query("SELECT AVG(pr.ratingValue) FROM ProductRating pr WHERE pr.product.id = :productId")
  Double getAverageRatingByProductId(@Param("productId") UUID productId);
}
