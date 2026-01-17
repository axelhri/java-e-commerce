package neora.mapper;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import neora.dto.OrderProductResponse;
import neora.dto.OrderResponse;
import neora.entity.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
  public OrderResponse toOrderResponse(
      UUID orderId, Set<UUID> productIds, BigDecimal totalAmount, UUID shippingAddress) {
    return new OrderResponse(orderId, productIds, totalAmount, shippingAddress);
  }

  public OrderProductResponse toOrderProductResponse(OrderItem orderItem) {
    return new OrderProductResponse(
        orderItem.getProduct().getId().toString(),
        orderItem.getProduct().getPrimaryImage().getImageUrl(),
        orderItem.getQuantity());
  }
}
