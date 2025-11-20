package ecom.unit.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.controller.CategoryController;
import ecom.dto.ApiResponse;
import ecom.dto.CategoryRequest;
import ecom.entity.Category;
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
      when(categoryService.createCategory(categoryRequest)).thenReturn(category);

      ResponseEntity<ApiResponse> response = categoryController.createCategory(categoryRequest);

      assertNotNull(response);
      assertTrue(response.getBody().success());
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals("Category created successfully.", response.getBody().message());
    }
  }
}
