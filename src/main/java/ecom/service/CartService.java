package ecom.service;

import ecom.entity.Cart;
import ecom.entity.User;
import ecom.exception.CartAlreadyExistsException;
import ecom.interfaces.CartServiceInterface;
import ecom.repository.CartRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CartService implements CartServiceInterface {

  private final CartRepository cartRepository;

  @Override
  public Cart createCart(User user) {

    if (user.getCart() != null) {
      throw new CartAlreadyExistsException(user.getEmail() + " cart already exists");
    }

    Cart cart = Cart.builder().user(user).build();
    user.setCart(cart);
    return cartRepository.save(cart);
  }
}
