package ecom.repository;

import ecom.entity.Product;
import ecom.entity.ProductRating;
import ecom.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRatingRepository extends JpaRepository<ProductRating, UUID> {
  Optional<ProductRating> findByUserAndProduct(User user, Product product);
}
