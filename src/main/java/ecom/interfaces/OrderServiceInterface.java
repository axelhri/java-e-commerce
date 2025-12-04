package ecom.interfaces;

import ecom.dto.CancelOrderRequest;
import ecom.dto.OrderRequest;
import ecom.dto.OrderResponse;
import ecom.entity.User;

public interface OrderServiceInterface {
  OrderResponse initiateOrder(User user, OrderRequest request);

  OrderResponse cancelOrder(User user, CancelOrderRequest request);
}
