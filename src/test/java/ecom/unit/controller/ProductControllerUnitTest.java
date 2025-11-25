package ecom.unit.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecom.config.JwtAuthenticationFilter;
import ecom.controller.ProductController;
import ecom.dto.ProductRequest;
import ecom.entity.Product;
import ecom.interfaces.ProductServiceInterface;
import ecom.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
  private Product product;
  private UUID categoryId = UUID.randomUUID();
  private UUID vendorId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    productRequest =
        new ProductRequest(
            "Black trench coat", 80000, "Black comfortable trench coat.", vendorId, categoryId);
    product =
        Product.builder()
            .name(productRequest.name())
            .price(productRequest.price())
            .description(productRequest.description())
            .build();
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
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Product created successfully"));

      verify(productService, times(1)).createProduct(any(ProductRequest.class));
    }

    @Test
    void createProductShouldReturn400BadRequestIfNameIsIncorrect() throws Exception {
      // Arrange
      productRequest =
          new ProductRequest("d", 5000, "Random product description.", vendorId, categoryId);

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
      productRequest = new ProductRequest("Random product name.", 5000, "d", vendorId, categoryId);

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
              "Football gloves", null, "Red and white football gloves.", vendorId, categoryId);

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
              "Black trench coat", 80000, "Black comfortable trench coat.", null, categoryId);

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
              "Black trench coat", 80000, "Black comfortable trench coat.", vendorId, null);

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
}
