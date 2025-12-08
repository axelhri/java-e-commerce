package ecom.unit.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecom.config.JwtAuthenticationFilter;
import ecom.controller.OrderController;
import ecom.dto.CancelOrderRequest;
import ecom.dto.OrderRequest;
import ecom.dto.OrderResponse;
import ecom.entity.User;
import ecom.exception.UnauthorizedAccess;
import ecom.interfaces.OrderServiceInterface;
import ecom.service.JwtService;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
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

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerUnitTest {
  @MockitoBean private OrderServiceInterface orderService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired private MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  private OrderResponse orderResponse;

  private User user;

  private OrderRequest orderRequest;

  @BeforeEach
  void setUp() {
    orderResponse =
        new OrderResponse(Set.of(UUID.randomUUID(), UUID.randomUUID()), new BigDecimal("50"));
    user = User.builder().email("test@example.com").password("Password123!").build();
    orderRequest = new OrderRequest(Set.of(UUID.randomUUID(), UUID.randomUUID()));
  }

  @Nested
  class initiateOrderUnitTest {
    @Test
    void should_order_successfully_and_return_201_created() throws Exception {
      // Arrange
      when(orderService.initiateOrder(user, orderRequest)).thenReturn(orderResponse);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/orders")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(orderRequest)))
          .andExpect(status().isCreated());
    }

    @Test
    void should_return_bad_request_if_request_is_empty() throws Exception {
      // Arrange
      OrderRequest invalidRequest = new OrderRequest(Set.of());
      when(orderService.initiateOrder(user, invalidRequest)).thenReturn(orderResponse);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/orders")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.productIds").value("You must order at least 1 product."));
    }

    @Test
    void should_return_bad_request_if_request_is_null() throws Exception {
      // Arrange
      OrderRequest invalidRequest = new OrderRequest(null);
      when(orderService.initiateOrder(user, invalidRequest)).thenReturn(orderResponse);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/orders")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.productIds").value("You must order at least 1 product."));
    }
  }

  @Nested
  class cancelOrderUnitTest {

    private CancelOrderRequest cancelRequest;

    @BeforeEach
    void setupCancel() {
      cancelRequest = new CancelOrderRequest(UUID.randomUUID());
    }

    @Test
    void should_cancel_order_successfully_and_return_200_ok() throws Exception {
      // Arrange
      when(orderService.cancelOrder(user, cancelRequest)).thenReturn(orderResponse);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/orders/cancel")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(cancelRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("Order cancelled successfully"));
    }

    @Test
    void should_return_bad_request_if_request_is_null() throws Exception {
      // Arrange
      CancelOrderRequest invalidRequest = new CancelOrderRequest(null);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/orders/cancel")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.orderId").value("Order ID must not be null."));
    }

    @Test
    void should_return_not_found_if_order_does_not_exist() throws Exception {
      // Arrange
      SecurityContextHolder.getContext()
          .setAuthentication(new TestingAuthenticationToken(user, null));

      when(orderService.cancelOrder(user, cancelRequest))
          .thenThrow(
              new ecom.exception.ResourceNotFoundException(
                  "Issue encountered while searching for this order."));

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/orders/cancel")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(cancelRequest)))
          .andExpect(status().isNotFound())
          .andExpect(
              jsonPath("$.message").value("Issue encountered while searching for this order."));
    }

    @Test
    void should_return_forbidden_if_user_is_not_owner() throws Exception {
      // Arrange
      SecurityContextHolder.getContext()
          .setAuthentication(new TestingAuthenticationToken(user, null));

      when(orderService.cancelOrder(any(User.class), eq(cancelRequest)))
          .thenThrow(new UnauthorizedAccess("You do not have the rights to perform this action."));

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/orders/cancel")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(cancelRequest)))
          .andExpect(status().isForbidden())
          .andExpect(
              jsonPath("$.message").value("You do not have the rights to perform this action."));
    }
  }
}
