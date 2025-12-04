package ecom.service;

import ecom.dto.CancelOrderRequest;
import ecom.dto.OrderRequest;
import ecom.dto.OrderResponse;
import ecom.entity.*;
import ecom.exception.ResourceNotFoundException;
import ecom.exception.UnauthorizedAccess;
import ecom.interfaces.OrderServiceInterface;
import ecom.mapper.OrderItemMapper;
import ecom.repository.CartItemRepository;
import ecom.repository.OrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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

  @Override
  @Transactional
  public OrderResponse initiateOrder(User user, OrderRequest request) {
    List<CartItem> foundItems = cartItemRepository.findAllById(request.productIds());

    for (CartItem cartItem : foundItems) {
      validateCartItemOwnership(user, cartItem);
    }

    Set<CartItem> cartItems = new HashSet<>(foundItems);

    Order order = Order.builder().user(user).build();

    Set<OrderItem> orderItems =
        cartItems.stream()
            .map(cartItem -> orderItemMapper.fromCartItem(cartItem, order))
            .collect(Collectors.toSet());

    order.getOrderItems().addAll(orderItems);

    orderRepository.save(order);

    cartItemRepository.deleteAll(cartItems);

    BigDecimal orderTotal = getOrderTotalAmount(orderItems);

    Set<UUID> productIds =
        orderItems.stream().map(item -> item.getProduct().getId()).collect(Collectors.toSet());

    return new OrderResponse(productIds, orderTotal);
  }

  @Override
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

    orderRepository.deleteById(request.orderId());

    BigDecimal orderTotal = getOrderTotalAmount(new HashSet<>(order.getOrderItems()));

    Set<UUID> productIds =
        order.getOrderItems().stream()
            .map(item -> item.getProduct().getId())
            .collect(Collectors.toSet());

    return new OrderResponse(productIds, orderTotal);
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
}
