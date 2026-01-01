package neora.interfaces;

import neora.dto.CategoryRequest;
import neora.dto.CategoryResponse;
import neora.entity.Category;
import java.util.List;

public interface CategoryServiceInterface {
  Category createCategory(CategoryRequest categoryRequest);

  List<CategoryResponse> getAllCategories();
}
