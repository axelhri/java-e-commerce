package ecom.controller;

import ecom.dto.ApiResponse;
import ecom.dto.ProductRequest;
import ecom.service.ProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

  private final ProductService productService;

  @PostMapping
  public ResponseEntity<ApiResponse> createProduct(@Valid @RequestBody ProductRequest dto) {
    productService.createProduct(dto);
    return ResponseEntity.ok(new ApiResponse(true, "Product created successfully"));
  }
}
