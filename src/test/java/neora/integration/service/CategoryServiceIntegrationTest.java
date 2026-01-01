package neora.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.UUID;
import neora.config.PostgresTestContainer;
import neora.dto.CategoryRequest;
import neora.entity.Category;
import neora.exception.ResourceAlreadyExistsException;
import neora.exception.ResourceNotFoundException;
import neora.mapper.CategoryMapper;
import neora.repository.CategoryRepository;
import neora.service.CategoryService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled
@SpringBootTest
public class CategoryServiceIntegrationTest extends PostgresTestContainer {
  @Autowired private CategoryService categoryService;
  @Autowired private CategoryRepository categoryRepository;
  @Autowired private CategoryMapper categoryMapper;

  private Category category;

  @Nested
  class createCategoryIntegrationTest {
    @Test
    void createCategoryShouldCreateCategorySuccessfully() {

      // Arrange
      CategoryRequest request = new CategoryRequest("Laptop", null);

      // Act
      category = categoryService.createCategory(request);

      // Assert
      assertNotNull(category);
      assertNotNull(category.getId());
      assertNotNull(category.getName());

      Category savedCategory = categoryRepository.findById(category.getId()).orElseThrow();

      assertEquals(savedCategory.getName(), category.getName());
    }

    @Test
    void createCategoryShouldThrowExceptionWhenCategoryAlreadyExists() {
      // Arrange
      CategoryRequest request = new CategoryRequest("Laptop", null);

      // Act & Assert
      assertThrows(
          ResourceAlreadyExistsException.class, () -> categoryService.createCategory(request));
    }

    @Test
    void createCategoryShouldThrowExceptionWhenCategoryIsNotFound() {
      // Arrange
      CategoryRequest request = new CategoryRequest("Pool", Set.of(UUID.randomUUID()));

      // Act & Assert
      assertThrows(ResourceNotFoundException.class, () -> categoryService.createCategory(request));
    }
  }
}
