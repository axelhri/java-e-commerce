package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.dto.CategoryRequest;
import ecom.dto.CategoryResponse;
import ecom.entity.Category;
import ecom.exception.ResourceAlreadyExistsException;
import ecom.exception.ResourceNotFoundException;
import ecom.mapper.CategoryMapper;
import ecom.repository.CategoryRepository;
import ecom.service.CategoryService;
import java.util.List;
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
import org.springframework.dao.DataIntegrityViolationException;

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
    void should_return_all_categories() {
      // Arrange
      Category category1 = Category.builder().id(UUID.randomUUID()).name("Electronics").build();
      Category category2 = Category.builder().id(UUID.randomUUID()).name("Books").build();
      when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

      // Act
      List<CategoryResponse> responses = categoryService.getAllCategories();

      // Assert
      assertNotNull(responses);
      assertEquals(2, responses.size());
      assertEquals("Electronics", responses.get(0).name());
      assertEquals("Books", responses.get(1).name());
    }
  }
}
