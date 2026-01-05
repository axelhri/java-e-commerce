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
import neora.dto.CancelOrderRequest;
import neora.dto.OrderRequest;
import neora.dto.OrderResponse;
import neora.dto.PaymentResponse;
import neora.entity.*;
import neora.exception.EmptyCartException;
import neora.exception.InsufficientStockException;
import neora.exception.ResourceNotFoundException;
import neora.exception.UnauthorizedAccess;
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
public class OrderService implements OrderServiceInterface {
  private final CartItemRepository cartItemRepository;
  private final OrderRepository orderRepository;
  private final OrderItemMapper orderItemMapper;
  private final StockServiceInterface stockService;
  private final OrderMapper orderMapper;
  private final StripeService stripeService;
  private final ShippingAddressServiceInterface shippingAddressService;

  @Override
  @Transactional
  public PaymentResponse initiateOrder(User user, OrderRequest request) throws StripeException {
    List<CartItem> foundItems = cartItemRepository.findAllById(request.productIds());

    if (foundItems.isEmpty()) throw new EmptyCartException("No products were found in cart.");

    for (CartItem cartItem : foundItems) {
      validateCartItemOwnership(user, cartItem);

      int currentStock = stockService.getCurrentStock(cartItem.getProduct());
      if (currentStock < cartItem.getQuantity()) {
        throw new InsufficientStockException(
            "Insufficient stock for product: " + cartItem.getProduct().getName());
      }
    }

    ShippingAddress shippingAddress =
        shippingAddressService.createShippingAddress(request.shippingAddress());

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

    BigDecimal total = getOrderTotalAmount(orderItems);

    PaymentIntent intent = stripeService.createPaymentIntent(order, total);

    order.setStripePaymentIntentId(intent.getId());
    Order savedOrder = orderRepository.save(order);

    return new PaymentResponse(buildOrderResponse(savedOrder), intent.getClientSecret());
  }

  @Override
  @Transactional
  public void confirmPayment(UUID orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    if (order.getStatus() == OrderStatus.PAID) {
      return;
    }

    order.setStatus(OrderStatus.PAID);
    orderRepository.save(order);

    for (OrderItem orderItem : order.getOrderItems()) {
      stockService.createStockMovement(
          orderItem.getProduct(), orderItem.getQuantity(), StockType.OUT, StockReason.SALE);
    }

    List<CartItem> itemsToRemove = cartItemRepository.findAllByCartUser(order.getUser());
    cartItemRepository.deleteAll(itemsToRemove);
  }

  @Override
  @Transactional
  public void markPaymentAsFailed(UUID orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    order.setStatus(OrderStatus.PAYMENT_FAILED);
    orderRepository.save(order);
  }

  @Override
  @Transactional
  public PaymentResponse retryPayment(User user, UUID orderId) throws StripeException {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

    if (!order.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedAccess("You do not have the rights to perform this action.");
    }

    if (order.getStatus() == OrderStatus.PAID) {
      throw new IllegalStateException("Order is already paid");
    }

    BigDecimal total = getOrderTotalAmount(new HashSet<>(order.getOrderItems()));

    PaymentIntent intent = stripeService.createPaymentIntent(order, total);

    order.setStripePaymentIntentId(intent.getId());
    order.setStatus(OrderStatus.PENDING);
    orderRepository.save(order);

    return new PaymentResponse(buildOrderResponse(order), intent.getClientSecret());
  }

  @Override
  @Transactional
  public OrderResponse cancelOrder(User user, CancelOrderRequest request) throws StripeException {
    Order order =
        orderRepository
            .findById(request.orderId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Issue encountered while searching for this order."));

    if (!order.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedAccess("You do not have the rights to perform this action.");
    }

    switch (order.getStatus()) {
      case PAID -> {
        RefundCreateParams params =
            RefundCreateParams.builder().setPaymentIntent(order.getStripePaymentIntentId()).build();
        Refund.create(params);

        order.setStatus(OrderStatus.REFUNDED);
        order
            .getOrderItems()
            .forEach(
                item ->
                    stockService.createStockMovement(
                        item.getProduct(), item.getQuantity(), StockType.IN, StockReason.RETURN));
      }
      case PENDING -> order.setStatus(OrderStatus.CANCELLED);
      case CANCELLED, REFUNDED ->
          throw new IllegalStateException("Order cannot be cancelled in current state");
    }

    orderRepository.save(order);
    return buildOrderResponse(order);
  }

  @Override
  public List<OrderResponse> getUserOrders(User user) {
    List<Order> orders = orderRepository.findByUser(user);
    return orders.stream().map(this::buildOrderResponse).collect(Collectors.toList());
  }

  @Override
  public OrderResponse getOrderById(User user, UUID orderId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Issue encountered while searching for this order."));

    if (!order.getUser().getId().equals(user.getId())) {
      throw new UnauthorizedAccess("You do not have the rights to perform this action.");
    }

    return buildOrderResponse(order);
  }

  @Override
  public List<OrderResponse> getUserCancelledOrders(User user) {
    List<Order> orders = orderRepository.findByUserAndStatus(user, OrderStatus.CANCELLED);
    return orders.stream().map(this::buildOrderResponse).collect(Collectors.toList());
  }

  private OrderResponse buildOrderResponse(Order order) {
    BigDecimal total = getOrderTotalAmount(new HashSet<>(order.getOrderItems()));
    Set<UUID> ids = extractProductIds(order.getOrderItems());
    return orderMapper.toOrderResponse(
        order.getId(), ids, total, order.getShippingAddress().getId());
  }

  private void validateCartItemOwnership(User user, CartItem cartItem) {
    if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
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
