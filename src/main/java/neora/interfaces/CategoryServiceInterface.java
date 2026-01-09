package neora.interfaces;

import java.util.List;
import neora.dto.CategoryRequest;
import neora.dto.CategoryResponse;
import neora.entity.Category;

public interface CategoryServiceInterface {
  Category createCategory(CategoryRequest categoryRequest);

  List<CategoryResponse> getAllCategories();
}
