package neora.service;

import neora.dto.CategoryRequest;
import neora.dto.CategoryResponse;
import neora.entity.Category;
import neora.exception.ResourceAlreadyExistsException;
import neora.exception.ResourceNotFoundException;
import neora.interfaces.CategoryServiceInterface;
import neora.mapper.CategoryMapper;
import neora.repository.CategoryRepository;
import java.util.List;
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

  @Override
  public List<CategoryResponse> getAllCategories() {
    return categoryRepository.findAll().stream()
        .map(category -> new CategoryResponse(category.getId(), category.getName()))
        .collect(Collectors.toList());
  }
}
