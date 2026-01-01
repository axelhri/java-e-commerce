package neora.mapper;

import neora.dto.OrderResponse;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
  public OrderResponse toOrderResponse(Set<UUID> productIds, BigDecimal totalAmount) {
    return new OrderResponse(productIds, totalAmount);
  }
}
