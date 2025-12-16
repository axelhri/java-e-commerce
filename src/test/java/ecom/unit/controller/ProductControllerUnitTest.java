package ecom.unit.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecom.config.JwtAuthenticationFilter;
import ecom.controller.ProductController;
import ecom.dto.ProductRequest;
import ecom.dto.ProductResponse;
import ecom.entity.Product;
import ecom.interfaces.ProductServiceInterface;
import ecom.service.JwtService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerUnitTest {

  @MockitoBean private ProductServiceInterface productService;

  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockitoBean private JwtService jwtService;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private ProductRequest productRequest;
  private ProductResponse productResponse;
  private Product product;
  private UUID categoryId = UUID.randomUUID();
  private UUID vendorId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    productRequest =
        new ProductRequest(
            "Black trench coat",
            80000,
            "Black comfortable trench coat.",
            100,
            vendorId,
            categoryId);
    product =
        Product.builder()
            .name(productRequest.name())
            .price(productRequest.price())
            .description(productRequest.description())
            .build();
    productResponse =
        new ProductResponse(UUID.randomUUID(), "Laptop", 1500, "16 inch blue laptop", 100);
  }

  @Nested
  class createProductTest {

    @Test
    void createProductShouldReturnCode200Ok() throws Exception {
      // Arrange
      when(productService.createProduct(productRequest)).thenReturn(product);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(productRequest)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.message").value("Product created successfully"));

      verify(productService, times(1)).createProduct(any(ProductRequest.class));
    }

    @Test
    void createProductShouldReturn400BadRequestIfNameIsIncorrect() throws Exception {
      // Arrange
      productRequest =
          new ProductRequest("d", 5000, "Random product description.", 100, vendorId, categoryId);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(productRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.name").value("size must be between 3 and 100"));

      verify(productService, never()).createProduct(any(ProductRequest.class));
    }

    @Test
    void createProductShouldReturn400BadRequestIfDescriptionIsIncorrect() throws Exception {
      // Arrange
      productRequest =
          new ProductRequest("Random product name.", 5000, "d", 100, vendorId, categoryId);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(productRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.description").value("size must be between 10 and 100"));

      verify(productService, never()).createProduct(any(ProductRequest.class));
    }

    @Test
    void createProductShouldReturn400BadRequestIfPriceIsNull() throws Exception {
      // Arrange
      productRequest =
          new ProductRequest(
              "Football gloves", null, "Red and white football gloves.", 100, vendorId, categoryId);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(productRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.price").value("Price is required."));

      verify(productService, never()).createProduct(any(ProductRequest.class));
    }

    @Test
    void createProductShouldReturn400BadRequestIfVendorIsMissing() throws Exception {
      // Arrange
      productRequest =
          new ProductRequest(
              "Black trench coat", 80000, "Black comfortable trench coat.", 100, null, categoryId);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(productRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.vendor").value("Vendor is required."));

      verify(productService, never()).createProduct(any(ProductRequest.class));
    }

    @Test
    void createProductShouldReturn400BadRequestIfCategoryIsMissing() throws Exception {
      // Arrange
      productRequest =
          new ProductRequest(
              "Black trench coat", 80000, "Black comfortable trench coat.", 100, vendorId, null);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(productRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.category").value("Category is required."));

      verify(productService, never()).createProduct(any(ProductRequest.class));
    }
  }

  @Nested
  class getAllProducts {
    @Test
    void should_get_all_products_paginated() throws Exception {
      // Arrange
      Page<ProductResponse> page = new PageImpl<>(List.of(productResponse));
      when(productService.getAllProducts(eq(null), any(Pageable.class))).thenReturn(page);

      // Act & Assert
      mockMvc
          .perform(get("/api/v1/products").param("page", "0").param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content").isArray())
          .andExpect(jsonPath("$.data.content[0].product_name").value("Laptop"))
          .andExpect(jsonPath("$.data.page").value(0))
          .andExpect(jsonPath("$.data.size").value(1));
    }

    @Test
    void should_get_products_by_category_paginated() throws Exception {
      // Arrange
      UUID categoryId = UUID.randomUUID();
      Page<ProductResponse> page = new PageImpl<>(List.of(productResponse));
      when(productService.getAllProducts(eq(categoryId), any(Pageable.class))).thenReturn(page);

      // Act & Assert
      mockMvc
          .perform(
              get("/api/v1/products")
                  .param("categoryId", categoryId.toString())
                  .param("page", "0")
                  .param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content").isArray())
          .andExpect(jsonPath("$.data.content[0].product_name").value("Laptop"));
    }

    @Test
    void should_return_empty_page_when_no_products_found() throws Exception {
      // Arrange
      when(productService.getAllProducts(any(), any(Pageable.class))).thenReturn(Page.empty());

      // Act & Assert
      mockMvc
          .perform(get("/api/v1/products"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content").isEmpty())
          .andExpect(jsonPath("$.data.totalElements").value(0));
    }
  }
}
