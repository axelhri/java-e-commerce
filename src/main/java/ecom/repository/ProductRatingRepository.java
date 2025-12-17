package ecom.repository;

import ecom.entity.Product;
import ecom.entity.ProductRating;
import ecom.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRatingRepository extends JpaRepository<ProductRating, UUID> {
  Optional<ProductRating> findByUserAndProduct(User user, Product product);

  @Query("SELECT AVG(pr.ratingValue) FROM ProductRating pr WHERE pr.product.vendor.id = :vendorId")
  Double getAverageRatingByVendorId(@Param("vendorId") UUID vendorId);
}
