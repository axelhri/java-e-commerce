package neora.repository;

import java.util.Optional;
import java.util.UUID;
import neora.entity.Category;
import neora.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository
    extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {
  Optional<Product> findBySlug(String slug);

  Page<Product> findAllProductsByCategory(Category category, Pageable pageable);
}
