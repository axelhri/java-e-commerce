package neora.interfaces;

import java.math.BigDecimal;
import java.util.List;
import neora.dto.CartItemResponse;
import neora.entity.Cart;
import neora.entity.User;

public interface CartServiceInterface {
  Cart createCart(User user);

  BigDecimal getCartTotalAmount(User user);

  List<CartItemResponse> getCartProducts(User user);

  Cart getUserCart(User user);

  void clearCart(User user);
}
