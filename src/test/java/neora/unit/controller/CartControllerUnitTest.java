package neora.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import neora.config.JwtAuthenticationFilter;
import neora.config.RateLimitingFilter;
import neora.controller.CartController;
import neora.interfaces.CartServiceInterface;
import neora.service.JwtService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerUnitTest {
  @MockitoBean private CartServiceInterface cartService;
  @MockitoBean private JwtService jwtService;

  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
  @MockitoBean private RateLimitingFilter rateLimitingFilter;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Nested
  class clearCart {
    @Test
    void should_clear_cart_successfully() throws Exception {
      // Act & Assert
      mockMvc.perform(delete("/api/v1/cart")).andExpect(status().isNoContent());

      verify(cartService, times(1)).clearCart(any());
    }
  }
}
