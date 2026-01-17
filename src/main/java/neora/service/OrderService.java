package neora.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.*;
import neora.entity.*;
import neora.exception.EmptyCartException;
import neora.exception.InsufficientStockException;
import neora.exception.ResourceNotFoundException;
import neora.exception.UnauthorizedAccess;
import neora.interfaces.EmailServiceInterface;
import neora.interfaces.OrderServiceInterface;
import neora.interfaces.ShippingAddressServiceInterface;
import neora.interfaces.StockServiceInterface;
import neora.mapper.OrderItemMapper;
import neora.mapper.OrderMapper;
import neora.model.OrderStatus;
import neora.model.StockReason;
import neora.model.StockType;
import neora.repository.CartItemRepository;
import neora.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class OrderService implements OrderServiceInterface {
  private final CartItemRepository cartItemRepository;
  private final OrderRepository orderRepository;
  private final OrderItemMapper orderItemMapper;
  private final StockServiceInterface stockService;
  private final OrderMapper orderMapper;
  private final StripeService stripeService;
  private final ShippingAddressServiceInterface shippingAddressService;
  private final EmailServiceInterface emailService;

  @Override
  @Transactional
  public PaymentResponse initiateOrder(User user, OrderRequest request) throws StripeException {
    log.info("Initiating order for user ID: {}", user.getId());
    List<CartItem> foundItems = cartItemRepository.findAllById(request.productIds());

    if (foundItems.isEmpty()) {
      log.warn("Order initiation failed: No products found in cart for user ID: {}", user.getId());
      throw new EmptyCartException("No products were found in cart.");
    }

    for (CartItem cartItem : foundItems) {
      validateCartItemOwnership(user, cartItem);

      int currentStock = stockService.getCurrentStock(cartItem.getProduct());
      if (currentStock < cartItem.getQuantity()) {
        log.warn(
            "Order initiation failed: Insufficient stock for product '{}'. Requested: {}, Available: {}",
            cartItem.getProduct().getName(),
            cartItem.getQuantity(),
            currentStock);
        throw new InsufficientStockException(
            "Insufficient stock for product: " + cartItem.getProduct().getName());
      }
    }

    ShippingAddress shippingAddress =
        shippingAddressService.createShippingAddress(request.shippingAddress());
    log.debug("Shipping address created with ID: {}", shippingAddress.getId());

    Order order =
        Order.builder()
            .user(user)
            .status(OrderStatus.PENDING)
            .orderItems(new ArrayList<>())
            .shippingAddress(shippingAddress)
            .build();

    Set<OrderItem> orderItems =
        foundItems.stream()
            .map(cartItem -> orderItemMapper.fromCartItem(cartItem, order))
            .collect(Collectors.toSet());

    order.getOrderItems().addAll(orderItems);

    orderRepository.save(order);
    log.info("Order created with ID: {} and status PENDING", order.getId());

    BigDecimal total = getOrderTotalAmount(orderItems);
    log.debug("Total order amount calculated: {}", total);

    PaymentIntent intent = stripeService.createPaymentIntent(order, total);
    log.info("Stripe PaymentIntent created with ID: {}", intent.getId());

    order.setStripePaymentIntentId(intent.getId());
    Order savedOrder = orderRepository.save(order);

    return new PaymentResponse(buildOrderResponse(savedOrder), intent.getClientSecret());
  }

  @Override
  @Transactional
  public void confirmPayment(UUID orderId) {
    log.info("Confirming payment for order ID: {}", orderId);
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> {
                  log.error("Order not found for ID: {}", orderId);
                  return new ResourceNotFoundException("Order not found");
                });

    if (order.getStatus() == OrderStatus.PAID) {
      log.info("Order ID: {} is already PAID, skipping confirmation", orderId);
      return;
    }

    order.setStatus(OrderStatus.PAID);
    orderRepository.save(order);
    log.info("Order ID: {} status updated to PAID", orderId);

    emailService.sendOrderPassedConfirmationEmail(order.getUser().getEmail(), order.getId());

    for (OrderItem orderItem : order.getOrderItems()) {
      stockService.createStockMovement(
          orderItem.getProduct(), orderItem.getQuantity(), StockType.OUT, StockReason.SALE);
    }
    log.debug("Stock movements created for order ID: {}", orderId);

    List<CartItem> itemsToRemove = cartItemRepository.findAllByCartUser(order.getUser());
    cartItemRepository.deleteAll(itemsToRemove);
    log.debug("Cart items removed for user ID: {}", order.getUser().getId());
  }

  @Override
  @Transactional
  public void markPaymentAsFailed(UUID orderId) {
    log.info("Marking payment as FAILED for order ID: {}", orderId);
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> {
                  log.error("Order not found for ID: {}", orderId);
                  return new ResourceNotFoundException("Order not found");
                });

    order.setStatus(OrderStatus.PAYMENT_FAILED);
    orderRepository.save(order);
    log.info("Order ID: {} status updated to PAYMENT_FAILED", orderId);
  }

  @Override
  @Transactional
  public PaymentResponse retryPayment(User user, UUID orderId) throws StripeException {
    log.info("Retrying payment for order ID: {} by user ID: {}", orderId, user.getId());
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> {
                  log.error("Order not found for ID: {}", orderId);
                  return new ResourceNotFoundException("Order not found");
                });

    if (!order.getUser().getId().equals(user.getId())) {
      log.warn(
          "Unauthorized access attempt to retry payment for order ID: {} by user ID: {}",
          orderId,
          user.getId());
      throw new UnauthorizedAccess("You do not have the rights to perform this action.");
    }

    if (order.getStatus() == OrderStatus.PAID) {
      log.warn("Retry payment failed: Order ID: {} is already PAID", orderId);
      throw new IllegalStateException("Order is already paid");
    }

    BigDecimal total = getOrderTotalAmount(new HashSet<>(order.getOrderItems()));

    PaymentIntent intent = stripeService.createPaymentIntent(order, total);
    log.info("New Stripe PaymentIntent created with ID: {}", intent.getId());

    order.setStripePaymentIntentId(intent.getId());
    order.setStatus(OrderStatus.PENDING);
    orderRepository.save(order);
    log.info("Order ID: {} status updated to PENDING with new PaymentIntent", orderId);

    emailService.sendOrderPassedConfirmationEmail(order.getUser().getEmail(), order.getId());

    return new PaymentResponse(buildOrderResponse(order), intent.getClientSecret());
  }

  @Override
  @Transactional
  public OrderResponse cancelOrder(User user, CancelOrderRequest request) throws StripeException {
    log.info("Cancelling order ID: {} for user ID: {}", request.orderId(), user.getId());
    Order order =
        orderRepository
            .findById(request.orderId())
            .orElseThrow(
                () -> {
                  log.error("Order not found for ID: {}", request.orderId());
                  return new ResourceNotFoundException(
                      "Issue encountered while searching for this order.");
                });

    if (!order.getUser().getId().equals(user.getId())) {
      log.warn(
          "Unauthorized access attempt to cancel order ID: {} by user ID: {}",
          request.orderId(),
          user.getId());
      throw new UnauthorizedAccess("You do not have the rights to perform this action.");
    }

    switch (order.getStatus()) {
      case PAID -> {
        log.info("Order ID: {} is PAID, initiating refund", order.getId());
        RefundCreateParams params =
            RefundCreateParams.builder().setPaymentIntent(order.getStripePaymentIntentId()).build();
        Refund.create(params);
        log.info("Refund initiated for order ID: {}", order.getId());

        order.setStatus(OrderStatus.REFUNDED);
        order
            .getOrderItems()
            .forEach(
                item ->
                    stockService.createStockMovement(
                        item.getProduct(), item.getQuantity(), StockType.IN, StockReason.RETURN));
        log.debug("Stock returned for refunded order ID: {}", order.getId());
      }
      case PENDING -> {
        order.setStatus(OrderStatus.CANCELLED);
        log.info("Order ID: {} status updated to CANCELLED", order.getId());
      }
      case CANCELLED, REFUNDED -> {
        log.warn(
            "Cancel order failed: Order ID: {} is already in state {}",
            order.getId(),
            order.getStatus());
        throw new IllegalStateException("Order cannot be cancelled in current state");
      }
    }

    orderRepository.save(order);
    emailService.sendOrderCancelledConfirmationEmail(order.getUser().getEmail(), order.getId());
    return buildOrderResponse(order);
  }

  @Override
  public List<OrderResponse> getUserOrders(User user) {
    log.info("Fetching orders for user ID: {}", user.getId());
    List<Order> orders = orderRepository.findByUser(user);
    log.info("Found {} orders for user ID: {}", orders.size(), user.getId());
    return orders.stream().map(this::buildOrderResponse).collect(Collectors.toList());
  }

  @Override
  public OrderResponse getOrderById(User user, UUID orderId) {
    log.info("Fetching order ID: {} for user ID: {}", orderId, user.getId());
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> {
                  log.error("Order not found for ID: {}", orderId);
                  return new ResourceNotFoundException(
                      "Issue encountered while searching for this order.");
                });

    if (!order.getUser().getId().equals(user.getId())) {
      log.warn(
          "Unauthorized access attempt to fetch order ID: {} by user ID: {}",
          orderId,
          user.getId());
      throw new UnauthorizedAccess("You do not have the rights to perform this action.");
    }

    return buildOrderResponse(order);
  }

  @Override
  public List<OrderResponse> getUserCancelledOrders(User user) {
    log.info("Fetching cancelled orders for user ID: {}", user.getId());
    List<Order> orders = orderRepository.findByUserAndStatus(user, OrderStatus.CANCELLED);
    log.info("Found {} cancelled orders for user ID: {}", orders.size(), user.getId());
    return orders.stream().map(this::buildOrderResponse).collect(Collectors.toList());
  }

  @Override
  public List<OrderProductResponse> getOrderProducts(UUID orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    return order.getOrderItems().stream()
        .map(orderMapper::toOrderProductResponse)
        .collect(Collectors.toList());
  }

  private OrderResponse buildOrderResponse(Order order) {
    BigDecimal total = getOrderTotalAmount(new HashSet<>(order.getOrderItems()));
    Set<UUID> ids = extractProductIds(order.getOrderItems());
    return orderMapper.toOrderResponse(
        order.getId(), ids, total, order.getShippingAddress().getId());
  }

  private void validateCartItemOwnership(User user, CartItem cartItem) {
    if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
      log.error(
          "Cart item ownership validation failed for user ID: {} and cart item ID: {}",
          user.getId(),
          cartItem.getId());
      throw new ResourceNotFoundException("Product not found in cart.");
    }
  }

  private BigDecimal getOrderTotalAmount(Set<OrderItem> items) {
    return items.stream()
        .map(
            item ->
                BigDecimal.valueOf(item.getProduct().getPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
  }

  private Set<UUID> extractProductIds(Collection<OrderItem> orderItems) {
    return orderItems.stream().map(item -> item.getProduct().getId()).collect(Collectors.toSet());
  }
}
