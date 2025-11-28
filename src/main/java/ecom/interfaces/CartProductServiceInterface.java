package ecom.interfaces;

import ecom.dto.AddToCartRequest;
import ecom.dto.CartItemResponse;
import ecom.entity.User;

public interface CartProductServiceInterface {
  CartItemResponse addProductToCart(User user, AddToCartRequest request);
}
