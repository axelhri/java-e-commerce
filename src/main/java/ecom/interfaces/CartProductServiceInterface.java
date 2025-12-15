package ecom.interfaces;

import ecom.dto.CartItemResponse;
import ecom.dto.ManageCartRequest;
import ecom.entity.User;
import java.math.BigDecimal;
import java.util.List;

public interface CartProductServiceInterface {
  CartItemResponse addProductToCart(User user, ManageCartRequest request);

  void removeProductFromCart(User user, ManageCartRequest request);

  BigDecimal getCartTotalAmount(User user);

  List<CartItemResponse> getCartProducts(User user);
}
