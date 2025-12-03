package ecom.interfaces;

import ecom.dto.OrderRequest;
import ecom.dto.OrderResponse;
import ecom.entity.User;

public interface OrderServiceInterface {
  OrderResponse createOrder(User user, OrderRequest request);
}
