package ecom.service;

import ecom.dto.OrderRequest;
import ecom.entity.*;
import ecom.exception.ResourceNotFoundException;
import ecom.interfaces.OrderServiceInterface;
import ecom.repository.CartItemRepository;
import ecom.repository.OrderRepository;
import ecom.repository.ProductRepository;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class OrderService implements OrderServiceInterface {
  private ProductRepository productRepository;
  private CartItemRepository cartItemRepository;
  private OrderRepository orderRepository;

  @Override
  public void createOrder(User user, OrderRequest request) {
    Set<CartItem> cartItems =
        request.productIds().stream()
            .map(
                id ->
                    cartItemRepository
                        .findById(id)
                        .orElseThrow(
                            () -> new ResourceNotFoundException("Product not found in cart.")))
            .collect(Collectors.toSet());

    cartItems.forEach(
        cartItem -> {
          if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Product not found in cart.");
          }
        });

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
  }

  public Cart getUserCart(User user) {
    return user.getCart();
  }
}
