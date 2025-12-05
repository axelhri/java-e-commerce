package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.dto.OrderRequest;
import ecom.dto.OrderResponse;
import ecom.entity.*;
import ecom.exception.ResourceNotFoundException;
import ecom.mapper.OrderItemMapper;
import ecom.repository.CartItemRepository;
import ecom.repository.OrderRepository;
import ecom.service.OrderService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTest {
  @Mock private CartItemRepository cartItemRepository;
  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemMapper orderItemMapper;
  @InjectMocks private OrderService orderService;

  private OrderRequest orderRequest;

  private OrderResponse orderResponse;

  private User user;

  private Cart cart;

  private CartItem cartItem;

  private Product product;

  private OrderItem orderItem;

  private Order order;

  @BeforeEach
  void setUp() {
    user =
        User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .password("Password123!")
            .build();
    cart = Cart.builder().user(user).build();
    product =
        Product.builder()
            .id(UUID.randomUUID())
            .name("Laptop")
            .description("16 inch blue laptop")
            .price(1500)
            .build();
    orderRequest = new OrderRequest(Set.of(UUID.randomUUID()));
    order = Order.builder().user(user).build();
    cartItem = CartItem.builder().product(product).cart(cart).quantity(5).build();
    orderItem = OrderItem.builder().product(product).order(order).build();
  }

  @Nested
  class initiateOrderUnitTest {

    @Test
    void should_initiate_order_successfully() {
      // Arrange
      when(cartItemRepository.findAllById(orderRequest.productIds())).thenReturn(List.of(cartItem));
      when(orderItemMapper.fromCartItem(eq(cartItem), any(Order.class))).thenReturn(orderItem);
      when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

      // Act
      OrderResponse response = orderService.initiateOrder(user, orderRequest);

      // Assert
      assertNotNull(response);
      assertEquals(Set.of(product.getId()), response.productsIds());
    }

    @Test
    void should_throw_exception_if_cart_item_ownership_validation_fails() {
      // Arrange
      User otherUser = User.builder().id(UUID.randomUUID()).email("other@example.com").build();

      Cart otherCart = Cart.builder().user(otherUser).build();

      CartItem invalidCartItem =
          CartItem.builder()
              .id(UUID.randomUUID())
              .product(product)
              .cart(otherCart)
              .quantity(3)
              .build();

      when(cartItemRepository.findAllById(orderRequest.productIds()))
          .thenReturn(List.of(invalidCartItem));

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> orderService.initiateOrder(user, orderRequest));

      // Assert
      assertEquals("Product not found in cart.", exception.getMessage());
      verify(orderRepository, never()).save(any(Order.class));
    }
  }
}
