package ecom.service;

import ecom.dto.CategoryRequest;
import ecom.entity.Category;
import ecom.exception.ResourceAlreadyExistsException;
import ecom.mapper.CategoryMapper;
import ecom.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  public Category createCategory(CategoryRequest categoryRequest) {
    Category category = categoryMapper.categoryToEntity(categoryRequest);

    try {
      return categoryRepository.save(category);
    } catch (DataIntegrityViolationException e) {
      throw new ResourceAlreadyExistsException("A category with this name already exists");
    }
  }
}
