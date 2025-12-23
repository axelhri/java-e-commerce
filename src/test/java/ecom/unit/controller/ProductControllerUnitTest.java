package ecom.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecom.config.JwtAuthenticationFilter;
import ecom.controller.ProductController;
import ecom.dto.AllProductsResponse;
import ecom.dto.ProductRequest;
import ecom.dto.ProductResponse;
import ecom.exception.ResourceNotFoundException;
import ecom.interfaces.ProductServiceInterface;
// Import ajouté
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerUnitTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private ProductServiceInterface productService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
  @Autowired private ObjectMapper objectMapper;

  private ProductRequest productRequest;
  private ProductResponse productResponse;
  private AllProductsResponse allProductsResponse;
  private UUID categoryId;
  private List<String> images;

  @BeforeEach
  void setUp() {
    categoryId = UUID.randomUUID();
    UUID vendorId = UUID.randomUUID();
    productRequest =
        new ProductRequest("Laptop", 1500, "16 inch blue laptop", 100, categoryId, vendorId);
    productResponse =
        new ProductResponse(UUID.randomUUID(), "Laptop", 1500, "16 inch blue laptop", 100, images);
    allProductsResponse = new AllProductsResponse(UUID.randomUUID(), "Laptop", 1500, 100, "url");
  }

  @Nested
  class CreateProduct {
    @Test
    void should_create_product_successfully() throws Exception {
      // Arrange
      MockMultipartFile productJson =
          new MockMultipartFile(
              "product", "", "application/json", objectMapper.writeValueAsBytes(productRequest));

      MockMultipartFile file =
          new MockMultipartFile("files", "image.jpg", "image/jpeg", "content".getBytes());

      when(productService.createProduct(any(ProductRequest.class), anyList()))
          .thenReturn(productResponse);

      // Act & Assert
      mockMvc
          .perform(multipart("/api/v1/products").file(productJson).file(file))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.product_name").value("Laptop"));
    }

    @Test
    void should_return_bad_request_when_creating_product_with_invalid_data() throws Exception {
      // Arrange
      ProductRequest invalidRequest = new ProductRequest("", -100, "", -10, null, null);

      MockMultipartFile productJson =
          new MockMultipartFile(
              "product", "", "application/json", objectMapper.writeValueAsBytes(invalidRequest));

      MockMultipartFile file =
          new MockMultipartFile("files", "image.jpg", "image/jpeg", "content".getBytes());

      // Act & Assert
      mockMvc
          .perform(multipart("/api/v1/products").file(productJson).file(file))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  class GetAllProducts {
    @Test
    void should_get_all_products_paginated() throws Exception {
      // Arrange
      Page<AllProductsResponse> page = new PageImpl<>(List.of(allProductsResponse));
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
      Page<AllProductsResponse> page = new PageImpl<>(List.of(allProductsResponse));
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

    @Test
    void should_use_default_pagination_when_params_are_missing() throws Exception {
      // Arrange
      Page<AllProductsResponse> page = new PageImpl<>(List.of(allProductsResponse));
      when(productService.getAllProducts(eq(null), any(Pageable.class))).thenReturn(page);

      // Act & Assert
      mockMvc
          .perform(get("/api/v1/products"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void should_return_bad_request_when_category_id_is_invalid() throws Exception {
      // Act & Assert
      mockMvc
          .perform(get("/api/v1/products").param("categoryId", "invalid-uuid-format"))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  class GetProductById {
    @Test
    void should_return_product_when_found() throws Exception {
      // Arrange
      UUID productId = UUID.randomUUID();
      ProductResponse foundProduct =
          new ProductResponse(productId, "Found Product", 100, "Desc", 10, images);
      when(productService.getProductById(productId)).thenReturn(foundProduct);

      // Act & Assert
      mockMvc
          .perform(get("/api/v1/products/{id}", productId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.product_id").value(productId.toString()))
          .andExpect(jsonPath("$.data.product_name").value("Found Product"));
    }

    @Test
    void should_return_not_found_when_product_does_not_exist() throws Exception {
      // Arrange
      UUID productId = UUID.randomUUID();
      when(productService.getProductById(productId))
          .thenThrow(new ResourceNotFoundException("Product not found"));

      // Act & Arrange
      mockMvc.perform(get("/api/v1/products/{id}", productId)).andExpect(status().isNotFound());
    }
  }
}
