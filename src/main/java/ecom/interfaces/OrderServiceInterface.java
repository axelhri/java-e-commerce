package ecom.interfaces;

import ecom.dto.OrderRequest;
import ecom.entity.User;

public interface OrderServiceInterface {
  void createOrder(User user, OrderRequest request);
}
