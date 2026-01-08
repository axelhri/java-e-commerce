package neora.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import neora.dto.CategoryRequest;
import neora.dto.CategoryResponse;
import neora.entity.Category;
import neora.exception.ResourceAlreadyExistsException;
import neora.exception.ResourceNotFoundException;
import neora.mapper.CategoryMapper;
import neora.repository.CategoryRepository;
import neora.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {
  @Spy private CategoryMapper categoryMapper;
  @Mock private CategoryRepository categoryRepository;
  @InjectMocks private CategoryService categoryService;

  private Category category;

  private CategoryRequest categoryRequest;
  private UUID parentId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    categoryRequest = new CategoryRequest("Ballon", Set.of(parentId));
    category = Category.builder().name(categoryRequest.name()).build();
  }

  @Nested
  class createCategoryUnitTest {

    @Test
    void createCategoryShouldCreateNewCategorySuccessfully() {
      // Arrange
      when(categoryMapper.categoryToEntity(categoryRequest)).thenReturn(category);
      when(categoryRepository.findById(parentId)).thenReturn(Optional.of(category));
      when(categoryRepository.save(category)).thenReturn(category);

      // Arrange
      Category categoryResponse = categoryService.createCategory(categoryRequest);

      // Assert
      assertEquals(categoryResponse.getName(), categoryRequest.name());
    }

    @Test
    void createCategoryShouldCreateCategoryWithoutParentsWhenParentIdsIsNull() {
      // Arrange
      CategoryRequest request = new CategoryRequest("Ballon", null);
      Category categoryNoParent = Category.builder().name(request.name()).build();

      when(categoryMapper.categoryToEntity(request)).thenReturn(categoryNoParent);
      when(categoryRepository.save(categoryNoParent)).thenReturn(categoryNoParent);

      // Act
      Category response = categoryService.createCategory(request);

      // Assert
      verify(categoryRepository, never()).findById(any());
      assertEquals("Ballon", response.getName());
      assertNull(response.getParentCategory());
    }

    @Test
    void createCategoryShouldReturnNotFoundExceptionIfParentCategoryDoesNotExist() {
      // Arrange
      when(categoryMapper.categoryToEntity(categoryRequest)).thenReturn(category);
      when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> categoryService.createCategory(categoryRequest));

      assertEquals("Parent category not found", exception.getMessage());
    }

    @Test
    void createCategoryShouldReturnExceptionIfCategoryAlreadyExists() {
      // Arrange
      when(categoryMapper.categoryToEntity(categoryRequest)).thenReturn(category);
      when(categoryRepository.findById(parentId)).thenReturn(Optional.of(category));
      when(categoryRepository.save(category)).thenThrow(new DataIntegrityViolationException(""));

      // Act & Assert
      ResourceAlreadyExistsException exception =
          assertThrows(
              ResourceAlreadyExistsException.class,
              () -> categoryService.createCategory(categoryRequest));

      assertEquals("A category with this name already exists", exception.getMessage());
    }
  }

  @Nested
  class GetAllCategories {
    @Test
    void should_return_all_root_categories() {
      // Arrange
      Category category1 = Category.builder().id(UUID.randomUUID()).name("Electronics").build();
      Category category2 = Category.builder().id(UUID.randomUUID()).name("Books").build();

      category2.setChildrenCategory(Set.of(category1));

      when(categoryRepository.findByParentCategoryIsNull()).thenReturn(List.of(category2));

      // Act
      List<CategoryResponse> responses = categoryService.getAllCategories();

      // Assert
      assertNotNull(responses);
      assertEquals(1, responses.size()); // only root categories
      assertEquals("Books", responses.get(0).name());
      assertEquals(1, responses.get(0).children().size());
      assertEquals("Electronics", responses.get(0).children().get(0).name());
    }
  }
}
