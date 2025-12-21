package ecom.unit.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecom.config.JwtAuthenticationFilter;
import ecom.controller.CartProductController;
import ecom.dto.CartItemResponse;
import ecom.dto.ManageCartRequest;
import ecom.entity.Product;
import ecom.entity.User;
import ecom.exception.ResourceNotFoundException;
import ecom.interfaces.CartProductServiceInterface;
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

@WebMvcTest(CartProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartProductControllerUnitTest {
  @MockitoBean private CartProductServiceInterface cartProductService;

  @MockitoBean private JwtService jwtService;

  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private ManageCartRequest request;

  private Product product;

  private User user;

  private CartItemResponse cartItemResponse;

  @BeforeEach
  void setUp() {
    user = User.builder().email("test@example.com").password("Password123!").build();
    product =
        Product.builder()
            .id(UUID.randomUUID())
            .name("Lighter")
            .description("Blue and red lighter.")
            .price(500)
            .build();
    request = new ManageCartRequest(product.getId(), 1);
    cartItemResponse =
        new CartItemResponse(product.getId(), product.getName(), 1, product.getPrice());
  }

  @Nested
  class addProductToCart {

    @Test
    void should_add_product_to_cart_and_return_201_created() throws Exception {
      // Arrange
      when(cartProductService.addProductToCart(user, request)).thenReturn(cartItemResponse);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/cart-items")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.status").value(201))
          .andExpect(jsonPath("$.message").value("Product added to cart successfully"));
    }

    @Test
    void should_not_add_product_if_validation_fails() throws Exception {
      // Arrange
      request = new ManageCartRequest(product.getId(), -32);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/cart-items")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.quantity").value("Quantity must be at least 1 or higher"));
    }

    @Test
    void should_not_add_product_if_product_is_not_found() throws Exception {
      // Arrange
      when(cartProductService.addProductToCart(any(), any()))
          .thenThrow(new ResourceNotFoundException("Product not found"));

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/cart-items")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Product not found"));
    }
  }

  @Nested
  class removeProductFromCart {

    @Test
    void should_remove_product_and_return_204_no_content() throws Exception {
      mockMvc
          .perform(
              delete("/api/v1/cart-items")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNoContent());
    }
  }
}
