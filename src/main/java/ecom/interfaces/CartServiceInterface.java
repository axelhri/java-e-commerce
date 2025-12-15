package ecom.interfaces;

import ecom.dto.CartItemResponse;
import ecom.entity.Cart;
import ecom.entity.User;
import java.math.BigDecimal;
import java.util.List;

public interface CartServiceInterface {
  Cart createCart(User user);

  BigDecimal getCartTotalAmount(User user);

  List<CartItemResponse> getCartProducts(User user);

  Cart getUserCart(User user);

  void clearCart(User user);
}
