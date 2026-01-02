package neora.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import neora.dto.ApiRestResponse;
import neora.dto.CategoryRequest;
import neora.dto.CategoryResponse;
import neora.interfaces.CategoryServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/categories")
@Tag(name = "Categories", description = "Endpoints for managing product categories")
public class CategoryController {
  private final CategoryServiceInterface categoryService;

  @Operation(
      summary = "Create a new category",
      description = "Creates a new product category. Requires ADMIN role.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Category created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied (Admin only)",
            content = @Content),
        @ApiResponse(
            responseCode = "409",
            description = "Category already exists",
            content = @Content)
      })
  @PostMapping
  public ResponseEntity<ApiRestResponse<CategoryRequest>> createCategory(
      @Valid @RequestBody CategoryRequest dto) {
    categoryService.createCategory(dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiRestResponse<>(
                Instant.now(), HttpStatus.CREATED.value(), "Category created successfully", dto));
  }

  @Operation(
      summary = "Get all categories",
      description = "Retrieves a list of all available product categories.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categories fetched successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class)))
      })
  @GetMapping
  public ResponseEntity<ApiRestResponse<List<CategoryResponse>>> getAllCategories() {
    List<CategoryResponse> categories = categoryService.getAllCategories();
    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Categories fetched successfully", categories));
  }
}
