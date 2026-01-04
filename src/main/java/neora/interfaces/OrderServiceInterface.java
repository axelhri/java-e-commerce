package neora.interfaces;

import com.stripe.exception.StripeException;
import java.util.List;
import java.util.UUID;
import neora.dto.CancelOrderRequest;
import neora.dto.OrderRequest;
import neora.dto.OrderResponse;
import neora.dto.PaymentResponse;
import neora.entity.User;

public interface OrderServiceInterface {
  PaymentResponse initiateOrder(User user, OrderRequest request) throws StripeException;

  OrderResponse cancelOrder(User user, CancelOrderRequest request) throws StripeException;

  List<OrderResponse> getUserOrders(User user);

  OrderResponse getOrderById(User user, UUID orderId);

  List<OrderResponse> getUserCancelledOrders(User user);

  void confirmPayment(UUID orderId);
}
