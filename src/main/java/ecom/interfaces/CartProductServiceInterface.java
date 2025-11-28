package ecom.interfaces;

import ecom.dto.CartItemResponse;
import ecom.dto.ManageCartRequest;
import ecom.entity.User;
import java.math.BigDecimal;

public interface CartProductServiceInterface {
  CartItemResponse addProductToCart(User user, ManageCartRequest request);

  void removeProductFromCart(User user, ManageCartRequest request);

  BigDecimal getCartTotalAmount(User user);
}
