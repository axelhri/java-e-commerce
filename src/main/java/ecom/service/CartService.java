package ecom.service;

import ecom.dto.CartItemResponse;
import ecom.entity.Cart;
import ecom.entity.CartItem;
import ecom.entity.User;
import ecom.exception.CartAlreadyExistsException;
import ecom.interfaces.CartServiceInterface;
import ecom.repository.CartItemRepository;
import ecom.repository.CartRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CartService implements CartServiceInterface {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;

  @Override
  public Cart createCart(User user) {

    if (user.getCart() != null) {
      throw new CartAlreadyExistsException(user.getEmail() + " cart already exists");
    }

    Cart cart = Cart.builder().user(user).build();
    user.setCart(cart);
    return cartRepository.save(cart);
  }

  @Override
  public BigDecimal getCartTotalAmount(User user) {
    List<CartItem> cartItems = cartItemRepository.findByCartId(getUserCart(user).getId());

    if (cartItems == null || cartItems.isEmpty()) {
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    int total =
        cartItems.stream()
            .mapToInt(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
    return BigDecimal.valueOf(total).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
  }

  @Override
  public List<CartItemResponse> getCartProducts(User user) {
    List<CartItem> cartItems = cartItemRepository.findByCartId(getUserCart(user).getId());

    if (cartItems == null || cartItems.isEmpty()) {
      return Collections.emptyList();
    }

    return cartItems.stream()
        .filter(item -> item.getProduct() != null)
        .map(
            item ->
                new CartItemResponse(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getProduct().getPrice()))
        .collect(Collectors.toList());
  }

  @Override
  public Cart getUserCart(User user) {
    return user.getCart();
  }

  @Override
  @Transactional
  public void clearCart(User user) {
    Cart cart = getUserCart(user);
    cartItemRepository.deleteByCartId(cart.getId());
  }
}
