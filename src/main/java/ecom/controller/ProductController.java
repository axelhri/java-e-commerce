package ecom.controller;

import ecom.dto.ApiResponse;
import ecom.dto.PagedResponse;
import ecom.dto.ProductRequest;
import ecom.dto.ProductResponse;
import ecom.interfaces.ProductServiceInterface;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

  private final ProductServiceInterface productService;

  @PostMapping
  public ResponseEntity<ApiResponse<ProductRequest>> createProduct(
      @Valid @RequestBody ProductRequest dto) {
    productService.createProduct(dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiResponse<>(
                Instant.now(), HttpStatus.CREATED.value(), "Product created successfully", dto));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAllProducts(
      @RequestParam(required = false) UUID categoryId, Pageable pageable) {
    Page<ProductResponse> page = productService.getAllProducts(categoryId, pageable);
    PagedResponse<ProductResponse> response =
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
