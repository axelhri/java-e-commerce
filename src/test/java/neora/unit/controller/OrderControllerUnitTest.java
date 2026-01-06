package neora.unit.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import neora.config.JwtAuthenticationFilter;
import neora.controller.OrderController;
import neora.dto.CancelOrderRequest;
import neora.dto.OrderRequest;
import neora.dto.OrderResponse;
import neora.dto.PaymentResponse;
import neora.dto.ShippingAddressRequest;
import neora.entity.User;
import neora.exception.UnauthorizedAccess;
import neora.interfaces.OrderServiceInterface;
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

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerUnitTest {
  @MockitoBean private OrderServiceInterface orderService;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired private MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  private OrderResponse orderResponse;
  private PaymentResponse paymentResponse;

  private User user;

  private OrderRequest orderRequest;

  @BeforeEach
  void setUp() {
    orderResponse =
        new OrderResponse(
            UUID.randomUUID(),
            Set.of(UUID.randomUUID(), UUID.randomUUID()),
            new BigDecimal("50"),
            UUID.randomUUID());
    paymentResponse = new PaymentResponse(orderResponse, "client_secret");
    user = User.builder().email("test@example.com").password("Password123!").build();
    ShippingAddressRequest shippingAddressRequest =
        new ShippingAddressRequest("John", "Doe", "123 Main St", "12345", "NY", "USA");
    orderRequest =
        new OrderRequest(Set.of(UUID.randomUUID(), UUID.randomUUID()), shippingAddressRequest);

    // Simulate authenticated user
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @Nested
  class initiateOrderUnitTest {
    @Test
    void should_order_successfully_and_return_201_created() throws Exception {
      // Arrange
      when(orderService.initiateOrder(any(User.class), eq(orderRequest)))
          .thenReturn(paymentResponse);

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
      ShippingAddressRequest shippingAddressRequest =
          new ShippingAddressRequest("John", "Doe", "123 Main St", "12345", "NY", "USA");
      OrderRequest invalidRequest = new OrderRequest(Set.of(), shippingAddressRequest);
      when(orderService.initiateOrder(any(User.class), eq(invalidRequest)))
          .thenReturn(paymentResponse);

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
      OrderRequest invalidRequest = new OrderRequest(null, null);
      when(orderService.initiateOrder(any(User.class), eq(invalidRequest)))
          .thenReturn(paymentResponse);

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
      when(orderService.cancelOrder(any(User.class), eq(cancelRequest))).thenReturn(orderResponse);

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
      when(orderService.cancelOrder(any(User.class), eq(cancelRequest)))
          .thenThrow(
              new neora.exception.ResourceNotFoundException(
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

  @Nested
  class getUserOrders {
    @Test
    void should_get_user_orders_successfully() throws Exception {
      // Arrange
      List<OrderResponse> orders = List.of(orderResponse);
      when(orderService.getUserOrders(any(User.class))).thenReturn(orders);

      // Act & Assert
      mockMvc
          .perform(get("/api/v1/orders"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data").isArray())
          .andExpect(jsonPath("$.data[0].order_price").value(50.00));
    }
  }

  @Test
  void should_get_user_cancelled_orders_successfully() throws Exception {
    // Arrange
    List<OrderResponse> cancelledOrders = List.of(orderResponse);
    when(orderService.getUserCancelledOrders(any(User.class))).thenReturn(cancelledOrders);

    // Act & Assert
    mockMvc
        .perform(get("/api/v1/orders/cancelled"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].order_price").value(50.00));
  }
}
