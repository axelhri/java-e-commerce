package ecom.service;

import ecom.dto.CancelOrderRequest;
import ecom.dto.OrderRequest;
import ecom.dto.OrderResponse;
import ecom.entity.*;
import ecom.exception.EmptyCartException;
import ecom.exception.InsufficientStockException;
import ecom.exception.ResourceNotFoundException;
import ecom.exception.UnauthorizedAccess;
import ecom.interfaces.OrderServiceInterface;
import ecom.interfaces.StockServiceInterface;
import ecom.mapper.OrderItemMapper;
import ecom.mapper.OrderMapper;
import ecom.model.OrderStatus;
import ecom.model.StockReason;
import ecom.model.StockType;
import ecom.repository.CartItemRepository;
import ecom.repository.OrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class OrderService implements OrderServiceInterface {
  private CartItemRepository cartItemRepository;
  private OrderRepository orderRepository;
  private OrderItemMapper orderItemMapper;
  private StockServiceInterface stockService;
  private OrderMapper orderMapper;

  @Override
  @Transactional
  public OrderResponse initiateOrder(User user, OrderRequest request) {
    List<CartItem> foundItems = cartItemRepository.findAllById(request.productIds());

    if (foundItems.isEmpty()) {
      throw new EmptyCartException("No products were found in cart.");
    }

    for (CartItem cartItem : foundItems) {
      validateCartItemOwnership(user, cartItem);

      int currentStock = stockService.getCurrentStock(cartItem.getProduct());
      if (currentStock < cartItem.getQuantity()) {
        throw new InsufficientStockException(
            "Insufficient stock for product: " + cartItem.getProduct().getName());
      }
    }

    Set<CartItem> cartItems = new HashSet<>(foundItems);

    Order order = Order.builder().user(user).build();

    Set<OrderItem> orderItems =
        cartItems.stream()
            .map(cartItem -> orderItemMapper.fromCartItem(cartItem, order))
            .collect(Collectors.toSet());

    order.getOrderItems().addAll(orderItems);

    Order savedOrder = orderRepository.save(order);

    for (OrderItem orderItem : orderItems) {
      stockService.createStockMovement(
          orderItem.getProduct(), orderItem.getQuantity(), StockType.OUT, StockReason.SALE);
    }

    cartItemRepository.deleteAll(cartItems);

    return buildOrderResponse(savedOrder);
  }

  @Override
  @Transactional
  public OrderResponse cancelOrder(User user, CancelOrderRequest request) {
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

    order.setStatus(OrderStatus.CANCELLED);

    Order savedOrder = orderRepository.save(order);

    for (OrderItem orderItem : order.getOrderItems()) {
      stockService.createStockMovement(
          orderItem.getProduct(), orderItem.getQuantity(), StockType.IN, StockReason.RETURN);
    }

    return buildOrderResponse(savedOrder);
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
    return orderMapper.toOrderResponse(ids, total);
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
