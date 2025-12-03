package ecom.service;

import ecom.dto.OrderRequest;
import ecom.dto.OrderResponse;
import ecom.entity.*;
import ecom.exception.ResourceNotFoundException;
import ecom.interfaces.OrderServiceInterface;
import ecom.repository.CartItemRepository;
import ecom.repository.OrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderService implements OrderServiceInterface {
  private CartItemRepository cartItemRepository;
  private OrderRepository orderRepository;

  @Override
  public OrderResponse createOrder(User user, OrderRequest request) {
    Set<CartItem> cartItems =
        request.productIds().stream()
            .map(
                id ->
                    cartItemRepository
                        .findById(id)
                        .orElseThrow(
                            () -> new ResourceNotFoundException("Product not found in cart.")))
            .peek(cartItem -> validateCartItemOwnership(user, cartItem))
            .collect(Collectors.toSet());

    Order order = Order.builder().user(user).build();

    Set<OrderItem> orderItems =
        cartItems.stream()
            .map(
                cartItem ->
                    OrderItem.builder()
                        .order(order)
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .build())
            .collect(Collectors.toSet());

    order.getOrderItems().addAll(orderItems);

    orderRepository.save(order);

    cartItemRepository.deleteAll(cartItems);

    BigDecimal orderTotal = getOrderTotalAmount(orderItems);

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
