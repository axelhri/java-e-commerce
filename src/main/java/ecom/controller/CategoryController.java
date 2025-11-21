package ecom.controller;

import ecom.dto.ApiResponse;
import ecom.dto.CategoryRequest;
import ecom.interfaces.CategoryServiceInterface;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {
  private final CategoryServiceInterface categoryService;

  @PostMapping
  public ResponseEntity<ApiResponse> createCategory(@Valid @RequestBody CategoryRequest dto) {
    categoryService.createCategory(dto);
    return ResponseEntity.ok(new ApiResponse(true, "Category created successfully."));
  }
}
