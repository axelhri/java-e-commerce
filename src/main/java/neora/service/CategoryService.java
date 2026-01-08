package neora.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.CategoryRequest;
import neora.dto.CategoryResponse;
import neora.entity.Category;
import neora.exception.ResourceAlreadyExistsException;
import neora.exception.ResourceNotFoundException;
import neora.interfaces.CategoryServiceInterface;
import neora.mapper.CategoryMapper;
import neora.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class CategoryService implements CategoryServiceInterface {
  private final CategoryRepository categoryRepository;
  private final CategoryMapper categoryMapper;

  @Override
  public Category createCategory(CategoryRequest categoryRequest) {
    log.info("Attempting to create category with name: {}", categoryRequest.name());
    Category category = categoryMapper.categoryToEntity(categoryRequest);

    if (categoryRequest.parentIds() != null && !categoryRequest.parentIds().isEmpty()) {
      log.debug("Resolving {} parent categories", categoryRequest.parentIds().size());
      Set<Category> parents =
          categoryRequest.parentIds().stream()
              .map(
                  id ->
                      categoryRepository
                          .findById(id)
                          .orElseThrow(
                              () -> {
                                log.error("Parent category not found for ID: {}", id);
                                return new ResourceNotFoundException("Parent category not found");
                              }))
              .collect(Collectors.toSet());

      category.setParentCategory(parents);
    }

    try {
      Category savedCategory = categoryRepository.save(category);
      log.info("Category created successfully with ID: {}", savedCategory.getId());
      return savedCategory;
    } catch (DataIntegrityViolationException e) {
      log.warn(
          "Category creation failed: A category with name '{}' already exists",
          categoryRequest.name());
      throw new ResourceAlreadyExistsException("A category with this name already exists");
    }
  }

  @Override
  public List<CategoryResponse> getAllCategories() {
    log.info("Fetching all root categories");

    List<Category> rootCategories = categoryRepository.findByParentCategoryIsNull();

    List<CategoryResponse> response =
        rootCategories.stream()
            .map(categoryMapper::mapToCategoryResponse)
            .collect(Collectors.toList());

    log.info("Found {} root categories", response.size());
    return response;
  }
}
