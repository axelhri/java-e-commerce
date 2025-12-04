package ecom.mapper;

import ecom.entity.CartItem;
import ecom.entity.Order;
import ecom.entity.OrderItem;
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
