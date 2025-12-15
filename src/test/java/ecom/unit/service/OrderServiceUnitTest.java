package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.dto.CancelOrderRequest;
import ecom.dto.OrderRequest;
import ecom.dto.OrderResponse;
import ecom.entity.*;
import ecom.exception.EmptyCartException;
import ecom.exception.InsufficientStockException;
import ecom.exception.ResourceNotFoundException;
import ecom.exception.UnauthorizedAccess;
import ecom.interfaces.StockServiceInterface;
import ecom.mapper.OrderItemMapper;
import ecom.model.OrderStatus;
import ecom.repository.CartItemRepository;
import ecom.repository.OrderRepository;
import ecom.service.OrderService;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {
  @Mock private CartItemRepository cartItemRepository;
  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemMapper orderItemMapper;
  @Mock private StockServiceInterface stockService;
  @InjectMocks private OrderService orderService;

  private OrderRequest orderRequest;

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

      assertEquals("Product not found in cart.", exception.getMessage());
      verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void should_throw_exception_if_cart_item_is_empty() {
      // Arrange
      when(cartItemRepository.findAllById(orderRequest.productIds()))
          .thenReturn(Collections.emptyList());

      // Act & Assert
      EmptyCartException exception =
          assertThrows(
              EmptyCartException.class, () -> orderService.initiateOrder(user, orderRequest));

      assertEquals("No products were found in cart.", exception.getMessage());
      verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void should_throw_exception_if_stock_is_insufficient_during_order_initiation() {
      // Arrange
      when(cartItemRepository.findAllById(orderRequest.productIds())).thenReturn(List.of(cartItem));
      when(stockService.getCurrentStock(product)).thenReturn(2);

      // Act & Assert
      InsufficientStockException exception =
          assertThrows(
              InsufficientStockException.class,
              () -> orderService.initiateOrder(user, orderRequest));

      assertEquals("Insufficient stock for product: " + product.getName(), exception.getMessage());
      verify(orderRepository, never()).save(any(Order.class));
      verify(stockService, never()).createStockMovement(any(), any(), any(), any());
    }
  }

  @Nested
  class cancelOrderUnitTest {
    private CancelOrderRequest cancelRequest;
    private Order existingOrder;
    private OrderItem existingOrderItem;

    @BeforeEach
    void setupCancelOrder() {
      cancelRequest = new CancelOrderRequest(UUID.randomUUID());

      existingOrder =
          Order.builder()
              .id(cancelRequest.orderId())
              .user(user)
              .status(OrderStatus.PENDING)
              .build();

      existingOrderItem =
          OrderItem.builder().product(product).order(existingOrder).quantity(5).build();

      existingOrder.getOrderItems().add(existingOrderItem);
    }

    @Test
    void should_cancel_order_successfully() {
      // Arrange
      when(orderRepository.findById(cancelRequest.orderId()))
          .thenReturn(Optional.of(existingOrder));
      when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

      // Act
      OrderResponse response = orderService.cancelOrder(user, cancelRequest);

      // Assert
      assertNotNull(response);
      assertEquals(Set.of(product.getId()), response.productsIds());
      assertEquals(new BigDecimal("75.00"), response.price());
      assertEquals(OrderStatus.CANCELLED, existingOrder.getStatus());

      verify(orderRepository).save(existingOrder);
    }

    @Test
    void should_throw_exception_if_order_not_found() {
      // Arrange
      when(orderRepository.findById(cancelRequest.orderId())).thenReturn(Optional.empty());

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class, () -> orderService.cancelOrder(user, cancelRequest));

      assertEquals("Issue encountered while searching for this order.", exception.getMessage());

      verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void should_throw_unauthorized_access_if_user_not_owner() {
      // Arrange
      User otherUser = User.builder().id(UUID.randomUUID()).email("other@example.com").build();

      Order someoneElsesOrder =
          Order.builder()
              .id(cancelRequest.orderId())
              .user(otherUser)
              .status(OrderStatus.PENDING)
              .build();

      when(orderRepository.findById(cancelRequest.orderId()))
          .thenReturn(Optional.of(someoneElsesOrder));

      // Act & Assert
      UnauthorizedAccess exception =
          assertThrows(
              UnauthorizedAccess.class, () -> orderService.cancelOrder(user, cancelRequest));

      assertEquals("You do not have the rights to perform this action.", exception.getMessage());

      verify(orderRepository, never()).save(any(Order.class));
    }
  }

  @Nested
  class GetUserOrders {

    @Test
    void should_return_user_orders_successfully() {
      // Arrange
      Product product2 = Product.builder().id(UUID.randomUUID()).name("Mouse").price(500).build();
      OrderItem orderItem2 = OrderItem.builder().product(product2).quantity(2).build();
      Order order2 =
          Order.builder().user(user).orderItems(new ArrayList<>(List.of(orderItem2))).build();
      orderItem2.setOrder(order2);

      order.setOrderItems(new ArrayList<>(List.of(orderItem)));

      List<Order> userOrders = List.of(order, order2);
      when(orderRepository.findByUser(user)).thenReturn(userOrders);

      // Act
      List<OrderResponse> responses = orderService.getUserOrders(user);

      // Assert
      assertNotNull(responses);
      assertEquals(2, responses.size());

      assertEquals(1, responses.get(0).productsIds().size());
      assertTrue(responses.get(0).productsIds().contains(product.getId()));
      assertEquals(new BigDecimal("15.00"), responses.get(0).price());

      assertEquals(1, responses.get(1).productsIds().size());
      assertTrue(responses.get(1).productsIds().contains(product2.getId()));
      assertEquals(new BigDecimal("10.00"), responses.get(1).price());
    }
  }
}
