package neora.mapper;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import neora.dto.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
  public OrderResponse toOrderResponse(Set<UUID> productIds, BigDecimal totalAmount) {
    return new OrderResponse(productIds, totalAmount);
  }
}
