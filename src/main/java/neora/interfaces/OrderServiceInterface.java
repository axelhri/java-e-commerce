package neora.interfaces;

import java.util.List;
import java.util.UUID;
import neora.dto.CancelOrderRequest;
import neora.dto.OrderRequest;
import neora.dto.OrderResponse;
import neora.entity.User;

public interface OrderServiceInterface {
  OrderResponse initiateOrder(User user, OrderRequest request);

  OrderResponse cancelOrder(User user, CancelOrderRequest request);

  List<OrderResponse> getUserOrders(User user);

  OrderResponse getOrderById(User user, UUID orderId);

  List<OrderResponse> getUserCancelledOrders(User user);
}
