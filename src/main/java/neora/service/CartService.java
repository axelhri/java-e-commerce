package neora.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.CartItemResponse;
import neora.entity.Cart;
import neora.entity.CartItem;
import neora.entity.User;
import neora.exception.CartAlreadyExistsException;
import neora.interfaces.CartServiceInterface;
import neora.repository.CartItemRepository;
import neora.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class CartService implements CartServiceInterface {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;

  @Override
  public Cart createCart(User user) {
    log.info("Attempting to create cart for user ID: {}", user.getId());

    if (user.getCart() != null) {
      log.warn("Cart creation failed: User {} already has a cart", user.getEmail());
      throw new CartAlreadyExistsException(user.getEmail() + " cart already exists");
    }

    Cart cart = Cart.builder().user(user).build();
    user.setCart(cart);
    Cart savedCart = cartRepository.save(cart);
    log.info(
        "Cart created successfully with ID: {} for user ID: {}", savedCart.getId(), user.getId());
    return savedCart;
  }

  @Override
  public BigDecimal getCartTotalAmount(User user) {
    log.debug("Calculating total amount for user ID: {}", user.getId());
    List<CartItem> cartItems = cartItemRepository.findByCartId(getUserCart(user).getId());

    if (cartItems == null || cartItems.isEmpty()) {
      log.debug("Cart is empty for user ID: {}, returning 0.00", user.getId());
      return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    int total =
        cartItems.stream()
            .mapToInt(item -> item.getProduct().getPrice() * item.getQuantity())
            .sum();
    BigDecimal totalAmount =
        BigDecimal.valueOf(total).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    log.debug("Total amount calculated for user ID {}: {}", user.getId(), totalAmount);
    return totalAmount;
  }

  @Override
  public List<CartItemResponse> getCartProducts(User user) {
    log.debug("Fetching cart products for user ID: {}", user.getId());
    List<CartItem> cartItems = cartItemRepository.findByCartId(getUserCart(user).getId());

    if (cartItems == null || cartItems.isEmpty()) {
      log.debug("No products found in cart for user ID: {}", user.getId());
      return Collections.emptyList();
    }

    List<CartItemResponse> response =
        cartItems.stream()
            .filter(item -> item.getProduct() != null)
            .map(
                item ->
                    new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getPrimaryImage().getImageUrl(),
                        item.getQuantity(),
                        item.getProduct().getPrice()))
            .collect(Collectors.toList());

    log.debug("Found {} products in cart for user ID: {}", response.size(), user.getId());
    return response;
  }

  @Override
  public Cart getUserCart(User user) {
    // This method is often used internally, so DEBUG level is appropriate
    // to avoid spamming logs if called frequently.
    log.trace("Retrieving cart for user ID: {}", user.getId());
    return user.getCart();
  }

  @Override
  @Transactional
  public void clearCart(User user) {
    log.info("Clearing cart for user ID: {}", user.getId());
    Cart cart = getUserCart(user);
    cartItemRepository.deleteByCartId(cart.getId());
    log.info("Cart cleared successfully for user ID: {}", user.getId());
  }
}
