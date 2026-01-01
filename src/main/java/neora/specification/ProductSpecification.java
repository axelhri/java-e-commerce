package neora.specification;

import java.util.UUID;
import neora.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

  public static Specification<Product> hasCategory(UUID categoryId) {
    return (root, query, cb) ->
        categoryId == null ? null : cb.equal(root.get("category").get("id"), categoryId);
  }

  public static Specification<Product> nameContains(String search) {
    return (root, query, cb) ->
        search == null
            ? null
            : cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
  }
}
