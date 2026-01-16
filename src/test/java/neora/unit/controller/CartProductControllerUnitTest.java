package neora.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import neora.config.JwtAuthenticationFilter;
import neora.config.RateLimitingFilter;
import neora.controller.CartProductController;
import neora.dto.CartItemResponse;
import neora.dto.ManageCartRequest;
import neora.entity.User;
import neora.exception.ResourceNotFoundException;
import neora.interfaces.CartProductServiceInterface;
import neora.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartProductControllerUnitTest {
  @MockitoBean private CartProductServiceInterface cartProductService;
  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
  @MockitoBean private JwtService jwtService;

  @MockitoBean private RateLimitingFilter rateLimitingFilter;

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private User user;
  private ManageCartRequest validRequest;
  private CartItemResponse cartItemResponse;

  @BeforeEach
  void setUp() {
    user = User.builder().id(UUID.randomUUID()).build();
    validRequest = new ManageCartRequest(UUID.randomUUID(), 1);
    cartItemResponse =
        new CartItemResponse(
            UUID.randomUUID(), validRequest.productId(), "Test", List.of(""), 1, 100);

    // Simulate authenticated user
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @Nested
  class AddProductToCart {
    @Test
    void should_add_product_to_cart_and_return_201_created() throws Exception {
      // Arrange
      when(cartProductService.addProductToCart(any(User.class), any(ManageCartRequest.class)))
          .thenReturn(cartItemResponse);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/cart-items")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validRequest)))
          .andExpect(status().isCreated());
    }

    @Test
    void should_not_add_product_if_product_is_not_found() throws Exception {
      // Arrange
      when(cartProductService.addProductToCart(any(User.class), any(ManageCartRequest.class)))
          .thenThrow(new ResourceNotFoundException("Product not found"));

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/cart-items")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validRequest)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  class RemoveProductFromCart {
    @Test
    void should_remove_product_and_return_204_no_content() throws Exception {
      // Arrange
      doNothing()
          .when(cartProductService)
          .removeProductFromCart(any(User.class), any(ManageCartRequest.class));

      // Act & Assert
      mockMvc
          .perform(
              delete("/api/v1/cart-items")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validRequest)))
          .andExpect(status().isNoContent());
    }
  }
}
