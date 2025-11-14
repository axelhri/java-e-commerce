package ecom.interfaces;

import ecom.dto.CategoryRequest;
import ecom.entity.Category;

public interface CategoryServiceInterface {
  Category createCategory(CategoryRequest categoryRequest);
}
