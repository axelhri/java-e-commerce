package ecom.unit.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecom.config.JwtAuthenticationFilter;
import ecom.controller.OrderController;
import ecom.dto.OrderRequest;
import ecom.dto.OrderResponse;
import ecom.entity.User;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerUnitTest {
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
      when(orderService.initiateOrder(user, orderRequest))
          .thenReturn(orderResponse);

      mockMvc
          .perform(
              post("/api/v1/orders")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(orderRequest)))
          .andExpect(status().isCreated());
    }
  }
}
