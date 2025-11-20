package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.dto.CategoryRequest;
import ecom.entity.Category;
import ecom.exception.ResourceNotFoundException;
import ecom.mapper.CategoryMapper;
import ecom.repository.CategoryRepository;
import ecom.service.CategoryService;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceUnitTest {
  @Mock private CategoryMapper categoryMapper;
  @Mock private CategoryRepository categoryRepository;
  @InjectMocks private CategoryService categoryService;

  private Category category;

  private CategoryRequest categoryRequest;
  private UUID parentId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    categoryRequest = new CategoryRequest("Ballon", Set.of(parentId));
  }

  @Nested
  class createCategoryUnitTest {
    @Test
    void createCategoryShouldReturnNotFoundExceptionIfParentCategoryDoesNotExist() {
      // Arrange
      when(categoryMapper.categoryToEntity(categoryRequest)).thenReturn(category);
      when(categoryRepository.findById(any())).thenReturn(Optional.empty());

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> categoryService.createCategory(categoryRequest));

      assertEquals("Parent category not found", exception.getMessage());
    }
  }
}
