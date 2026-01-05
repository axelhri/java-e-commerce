package neora.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import java.math.BigDecimal;
import java.util.*;
import neora.dto.CancelOrderRequest;
import neora.dto.OrderRequest;
import neora.dto.OrderResponse;
import neora.dto.PaymentResponse;
import neora.dto.ShippingAddressRequest;
import neora.entity.*;
import neora.exception.EmptyCartException;
import neora.exception.InsufficientStockException;
import neora.exception.ResourceNotFoundException;
import neora.exception.UnauthorizedAccess;
import neora.interfaces.ShippingAddressServiceInterface;
import neora.interfaces.StockServiceInterface;
import neora.mapper.OrderItemMapper;
import neora.mapper.OrderMapper;
import neora.model.OrderStatus;
import neora.repository.CartItemRepository;
import neora.repository.OrderRepository;
import neora.service.OrderService;
import neora.service.StripeService;
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
  @Mock private OrderMapper orderMapper;
  @Mock private StripeService stripeService;
  @Mock private ShippingAddressServiceInterface shippingAddressService;
  @InjectMocks private OrderService orderService;

  private OrderRequest orderRequest;

  private User user;

  private Cart cart;

  private CartItem cartItem;

  private Product product;

  private OrderItem orderItem;

  private Order order;

  private ShippingAddress shippingAddress;

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
    ShippingAddressRequest shippingAddressRequest =
        new ShippingAddressRequest("John", "Doe", "123 Main St", "12345", "NY", "USA");
    orderRequest = new OrderRequest(Set.of(UUID.randomUUID()), shippingAddressRequest);
    shippingAddress = ShippingAddress.builder().id(UUID.randomUUID()).build();
    order = Order.builder().user(user).shippingAddress(shippingAddress).build();
    cartItem = CartItem.builder().product(product).cart(cart).quantity(5).build();
    orderItem = OrderItem.builder().product(product).order(order).quantity(5).build();
  }

  @Nested
  class initiateOrderUnitTest {

    @Test
    void should_initiate_order_successfully() throws StripeException {
      // Arrange
      PaymentIntent paymentIntent = mock(PaymentIntent.class);
      when(paymentIntent.getId()).thenReturn("pi_12345");
      when(paymentIntent.getClientSecret()).thenReturn("secret_12345");

      when(cartItemRepository.findAllById(orderRequest.productIds())).thenReturn(List.of(cartItem));
      when(shippingAddressService.createShippingAddress(any(ShippingAddressRequest.class)))
          .thenReturn(shippingAddress);
      when(orderItemMapper.fromCartItem(eq(cartItem), any(Order.class))).thenReturn(orderItem);
      when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
      when(stockService.getCurrentStock(product)).thenReturn(100);
      when(stripeService.createPaymentIntent(any(Order.class), any(BigDecimal.class)))
          .thenReturn(paymentIntent);
      when(orderMapper.toOrderResponse(any(), any(), any(), any()))
          .thenReturn(
              new OrderResponse(
                  UUID.randomUUID(),
                  Set.of(product.getId()),
                  new BigDecimal("75.00"),
                  shippingAddress.getId()));

      // Act
      PaymentResponse response = orderService.initiateOrder(user, orderRequest);

      // Assert
      assertNotNull(response);
      assertEquals("secret_12345", response.clientSecret());
      assertEquals(Set.of(product.getId()), response.order().productsIds());
      assertEquals(shippingAddress.getId(), response.order().shippingAddress());
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
              .shippingAddress(shippingAddress)
              .build();

      existingOrderItem =
          OrderItem.builder().product(product).order(existingOrder).quantity(5).build();

      existingOrder.getOrderItems().add(existingOrderItem);
    }

    @Test
    void should_cancel_order_successfully() throws StripeException {
      // Arrange
      when(orderRepository.findById(cancelRequest.orderId()))
          .thenReturn(Optional.of(existingOrder));
      when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
      when(orderMapper.toOrderResponse(any(), any(), any(), any()))
          .thenReturn(
              new OrderResponse(
                  UUID.randomUUID(),
                  Set.of(product.getId()),
                  new BigDecimal("75.00"),
                  shippingAddress.getId()));

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
          Order.builder()
              .user(user)
              .orderItems(new ArrayList<>(List.of(orderItem2)))
              .shippingAddress(shippingAddress)
              .build();
      orderItem2.setOrder(order2);

      order.setOrderItems(new ArrayList<>(List.of(orderItem)));

      List<Order> userOrders = List.of(order, order2);
      when(orderRepository.findByUser(user)).thenReturn(userOrders);
      when(orderMapper.toOrderResponse(any(), any(), any(), any()))
          .thenReturn(
              new OrderResponse(
                  UUID.randomUUID(),
                  Set.of(product.getId()),
                  new BigDecimal("15.00"),
                  shippingAddress.getId()),
              new OrderResponse(
                  UUID.randomUUID(),
                  Set.of(product2.getId()),
                  new BigDecimal("10.00"),
                  shippingAddress.getId()));

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

    @Test
    void should_return_empty_list_when_user_has_no_orders() {
      // Arrange
      when(orderRepository.findByUser(user)).thenReturn(Collections.emptyList());

      // Act
      List<OrderResponse> responses = orderService.getUserOrders(user);

      // Assert
      assertNotNull(responses);
      assertTrue(responses.isEmpty());
    }
  }

  @Nested
  class GetOrderById {
    @Test
    void should_throw_exception_if_order_not_found() {
      // Arrange
      UUID randomId = UUID.randomUUID();
      when(orderRepository.findById(randomId)).thenReturn(Optional.empty());

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class, () -> orderService.getOrderById(user, randomId));

      assertEquals("Issue encountered while searching for this order.", exception.getMessage());
    }

    @Test
    void should_throw_unauthorized_access_if_user_not_owner() {
      // Arrange
      User otherUser = User.builder().id(UUID.randomUUID()).email("other@example.com").build();
      Order otherOrder = Order.builder().id(UUID.randomUUID()).user(otherUser).build();

      UUID otherOrderId = otherOrder.getId();

      when(orderRepository.findById(otherOrderId)).thenReturn(Optional.of(otherOrder));

      // Act & Assert
      UnauthorizedAccess exception =
          assertThrows(
              UnauthorizedAccess.class, () -> orderService.getOrderById(user, otherOrderId));

      assertEquals("You do not have the rights to perform this action.", exception.getMessage());
    }
  }

  @Nested
  class GetUserCancelledOrders {
    @Test
    void should_return_user_cancelled_orders_successfully() {
      // Arrange
      Order cancelledOrder =
          Order.builder()
              .id(UUID.randomUUID())
              .user(user)
              .status(OrderStatus.CANCELLED)
              .orderItems(new ArrayList<>(List.of(orderItem)))
              .shippingAddress(shippingAddress)
              .build();
      orderItem.setOrder(cancelledOrder);

      when(orderRepository.findByUserAndStatus(user, OrderStatus.CANCELLED))
          .thenReturn(List.of(cancelledOrder));

      when(orderMapper.toOrderResponse(any(), any(), any(), any()))
          .thenReturn(
              new OrderResponse(
                  UUID.randomUUID(),
                  Set.of(product.getId()),
                  new BigDecimal("75.00"),
                  shippingAddress.getId()));

      // Act
      List<OrderResponse> responses = orderService.getUserCancelledOrders(user);

      // Assert
      assertNotNull(responses);
      assertEquals(1, responses.size());
      assertEquals(new BigDecimal("75.00"), responses.get(0).price());
      verify(orderRepository).findByUserAndStatus(user, OrderStatus.CANCELLED);
    }
  }
}
