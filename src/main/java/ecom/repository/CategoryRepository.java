package ecom.repository;

import ecom.entity.Category;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
  boolean existsByName(String name);
}
