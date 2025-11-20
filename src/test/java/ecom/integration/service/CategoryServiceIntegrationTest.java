package ecom.integration.service;

import static org.junit.jupiter.api.Assertions.*;

import ecom.config.PostgresTestContainer;
import ecom.dto.CategoryRequest;
import ecom.entity.Category;
import ecom.mapper.CategoryMapper;
import ecom.repository.CategoryRepository;
import ecom.service.CategoryService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CategoryServiceIntegrationTest extends PostgresTestContainer {
  @Autowired private CategoryService categoryService;
  @Autowired private CategoryRepository categoryRepository;
  @Autowired private CategoryMapper categoryMapper;

  private Category category;

  @BeforeEach
  void setUp() {
    category = Category.builder().name("Black Friday").build();
    categoryRepository.save(category);
  }

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
  }
}
