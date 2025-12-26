package ecom.controller;

import ecom.dto.*;
import ecom.interfaces.ProductServiceInterface;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

  private final ProductServiceInterface productService;

  @PostMapping
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
      @Valid @RequestPart("product") ProductRequest dto,
      @RequestPart("files") List<MultipartFile> files)
      throws IOException {
    ProductResponse product = productService.createProduct(dto, files);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiResponse<>(
                Instant.now(),
                HttpStatus.CREATED.value(),
                "Product created successfully",
                product));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PagedResponse<AllProductsResponse>>> getAllProducts(
      @RequestParam(required = false) UUID categoryId,
      @RequestParam(required = false) String search,
      Pageable pageable) {
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
        new ApiResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Products fetched successfully", response));
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID productId) {
    ProductResponse product = productService.getProductById(productId);
    return ResponseEntity.ok(
        new ApiResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Product fetched successfully", product));
  }
}
