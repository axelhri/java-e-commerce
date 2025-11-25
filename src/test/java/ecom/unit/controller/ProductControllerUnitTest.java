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

  @BeforeEach
  void setUp() {
    productRequest =
        new ProductRequest(
            "Black trench coat",
            80000,
            "Black comfortable trench coat.",
            UUID.randomUUID(),
            UUID.randomUUID());
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
      when(productService.createProduct(productRequest)).thenReturn(product);

      mockMvc
          .perform(
              post("/api/v1/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(productRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.success").value(true))
          .andExpect(jsonPath("$.message").value("Product created successfully"));
    }
  }
}
