package ecom.service;

import ecom.dto.CategoryRequest;
import ecom.entity.Category;
import ecom.exception.ResourceAlreadyExistsException;
import ecom.exception.ResourceNotFoundException;
import ecom.interfaces.CategoryServiceInterface;
import ecom.mapper.CategoryMapper;
import ecom.repository.CategoryRepository;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CategoryService implements CategoryServiceInterface {
  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  @Override
  public Category createCategory(CategoryRequest categoryRequest) {
    Category category = categoryMapper.categoryToEntity(categoryRequest);

    if (categoryRequest.parentIds() != null && !categoryRequest.parentIds().isEmpty()) {
      Set<Category> parents =
          categoryRequest.parentIds().stream()
              .map(
                  id ->
                      categoryRepository
                          .findById(id)
                          .orElseThrow(
                              () -> new ResourceNotFoundException("Parent category not found")))
              .collect(Collectors.toSet());

      category.setParentCategory(parents);
    }

    try {
      return categoryRepository.save(category);
    } catch (DataIntegrityViolationException e) {
      throw new ResourceAlreadyExistsException("A category with this name already exists");
    }
  }
}
