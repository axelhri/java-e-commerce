package neora.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import neora.dto.*;
import neora.interfaces.ProductServiceInterface;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/products")
@Tag(name = "Products", description = "Endpoints for managing products")
public class ProductController {

  private final ProductServiceInterface productService;

  @Operation(
      summary = "Create a new product",
      description = "Creates a new product with images. Requires ADMIN role.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied (Admin only)",
            content = @Content)
      })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiRestResponse<ProductResponse>> createProduct(
      @Parameter(description = "Product details in JSON format", required = true)
          @Valid
          @RequestPart("product")
          ProductRequest dto,
      @Parameter(description = "List of product images", required = false) @RequestPart("files")
          List<MultipartFile> files)
      throws IOException {
    ProductResponse product = productService.createProduct(dto, files);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiRestResponse<>(
                Instant.now(),
                HttpStatus.CREATED.value(),
                "Product created successfully",
                product));
  }

  @Operation(
      summary = "Get all products",
      description =
          "Retrieves a paginated list of products, optionally filtered by category or search term.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products fetched successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class)))
      })
  @GetMapping
  public ResponseEntity<ApiRestResponse<PagedResponse<AllProductsResponse>>> getAllProducts(
      @Parameter(description = "Filter by category ID") @RequestParam(required = false)
          UUID categoryId,
      @Parameter(description = "Search term for product name") @RequestParam(required = false)
          String search,
      @Parameter(description = "Pagination information") Pageable pageable) {
    Page<AllProductsResponse> page = productService.getAllProducts(categoryId, search, pageable);
    PagedResponse<AllProductsResponse> response =
        new PagedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast());

    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Products fetched successfully", response));
  }

  @GetMapping("/category")
  public ResponseEntity<ApiRestResponse<PagedResponse<AllProductsResponse>>> getProductsByCategory(
      @Parameter(description = "Filter by category ID") @RequestParam(required = false)
          UUID categoryId,
      @Parameter(description = "Pagination information") Pageable pageable) {
    Page<AllProductsResponse> page = productService.getProductsByCategory(categoryId, pageable);
    PagedResponse<AllProductsResponse> response =
        new PagedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isLast());

    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Products fetched successfully", response));
  }

  @Operation(
      summary = "Get product by ID",
      description = "Retrieves details of a specific product by its ID.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product fetched successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
      })
  @GetMapping("/{productId}")
  public ResponseEntity<ApiRestResponse<ProductResponse>> getProductById(
      @Parameter(description = "Product unique identifier", required = true) @PathVariable
          UUID productId) {
    ProductResponse product = productService.getProductById(productId);
    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Product fetched successfully", product));
  }

  @Operation(
      summary = "Get product by slug",
      description = "Retrieves details of a specific product by its slug.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product fetched successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
      })
  @GetMapping("slug/{slug}")
  public ResponseEntity<ApiRestResponse<ProductResponse>> getProductBySlug(
      @Parameter(description = "Product slug", required = true) @PathVariable String slug) {
    ProductResponse product = productService.getProductBySlug(slug);
    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Product fetched successfully", product));
  }
}
