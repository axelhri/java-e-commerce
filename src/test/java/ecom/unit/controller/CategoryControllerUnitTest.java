package ecom.unit.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.controller.CategoryController;
import ecom.dto.ApiResponse;
import ecom.dto.CategoryRequest;
import ecom.entity.Category;
import ecom.exception.ResourceAlreadyExistsException;
import ecom.exception.ResourceNotFoundException;
import ecom.service.CategoryService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerUnitTest {
  @Mock private CategoryService categoryService;
  @InjectMocks private CategoryController categoryController;

  private CategoryRequest categoryRequest;

  private UUID parentId = UUID.randomUUID();

  private Category category;

  @BeforeEach
  void setUp() {
    categoryRequest = new CategoryRequest("Ballon", Set.of(parentId));
    category = Category.builder().name(categoryRequest.name()).build();
  }

  @Nested
  class createCategoryUnitTest {

    @Test
    void createCategoryShouldReturn200Ok() {
      // Arrange
      when(categoryService.createCategory(categoryRequest)).thenReturn(category);

      // Act
      ResponseEntity<ApiResponse<CategoryRequest>> response =
          categoryController.createCategory(categoryRequest);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertEquals("Category created successfully", response.getBody().message());
    }

    @Test
    void createCategoryShouldThrowExceptionWhenCategoryAlreadyExists() {
      // Arrange
      categoryRequest = new CategoryRequest("Ballon!!", Set.of(parentId));

      doThrow(new ResourceAlreadyExistsException("A category with this name already exists"))
          .when(categoryService)
          .createCategory(categoryRequest);

      // Act & Assert
      ResourceAlreadyExistsException exception =
          assertThrows(
              ResourceAlreadyExistsException.class,
              () -> categoryController.createCategory(categoryRequest));

      assertEquals("A category with this name already exists", exception.getMessage());
    }

    @Test
    void createCategoryShouldThrowExceptionWhenParentCategoryIsNotFound() {
      doThrow(new ResourceNotFoundException("Parent category not found"))
          .when(categoryService)
          .createCategory(categoryRequest);

      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> categoryController.createCategory(categoryRequest));

      assertEquals("Parent category not found", exception.getMessage());
    }
  }
}
