package neora.interfaces;

import neora.dto.CartItemResponse;
import neora.dto.ManageCartRequest;
import neora.entity.User;

public interface CartProductServiceInterface {
  CartItemResponse addProductToCart(User user, ManageCartRequest request);

  void removeProductFromCart(User user, ManageCartRequest request);
}
