package ecom.interfaces;

import ecom.entity.Cart;
import ecom.entity.User;

public interface CartServiceInterface {
  Cart createCart(User user);
}
