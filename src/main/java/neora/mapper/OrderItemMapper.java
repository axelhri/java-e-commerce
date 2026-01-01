package neora.mapper;

import neora.entity.CartItem;
import neora.entity.Order;
import neora.entity.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {

  public OrderItem fromCartItem(CartItem cartItem, Order order) {
    return OrderItem.builder()
        .order(order)
        .product(cartItem.getProduct())
        .quantity(cartItem.getQuantity())
        .build();
  }
}
