package neora.interfaces;

import com.stripe.exception.StripeException;
import java.util.List;
import java.util.UUID;
import neora.dto.*;
import neora.entity.User;

public interface OrderServiceInterface {
  PaymentResponse initiateOrder(User user, OrderRequest request) throws StripeException;

  void confirmPayment(UUID orderId);

  void markPaymentAsFailed(UUID orderId);

  PaymentResponse retryPayment(User user, UUID orderId) throws StripeException;

  OrderResponse cancelOrder(User user, CancelOrderRequest request) throws StripeException;

  List<OrderResponse> getUserOrders(User user);

  OrderResponse getOrderById(User user, UUID orderId);

  List<OrderResponse> getUserCancelledOrders(User user);

  List<OrderProductResponse> getOrderProducts(UUID orderId);
}
