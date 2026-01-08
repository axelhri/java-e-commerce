package neora.repository;

import java.util.List;
import java.util.UUID;
import neora.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
  boolean existsByName(String name);

  List<Category> findByParentCategoryIsNull();
}
