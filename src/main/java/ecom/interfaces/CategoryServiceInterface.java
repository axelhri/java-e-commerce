package ecom.interfaces;

import ecom.dto.CategoryRequest;
import ecom.dto.CategoryResponse;
import ecom.entity.Category;
import java.util.List;

public interface CategoryServiceInterface {
  Category createCategory(CategoryRequest categoryRequest);

  List<CategoryResponse> getAllCategories();
}
