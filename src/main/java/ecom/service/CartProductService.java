package ecom.service;

import ecom.dto.CartItemResponse;
import ecom.dto.ManageCartRequest;
import ecom.entity.Cart;
import ecom.entity.CartItem;
import ecom.entity.Product;
import ecom.entity.User;
import ecom.exception.InsufficientStockException;
import ecom.exception.ResourceNotFoundException;
import ecom.interfaces.CartProductServiceInterface;
import ecom.interfaces.StockServiceInterface;
import ecom.repository.CartItemRepository;
import ecom.repository.ProductRepository;
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
public class CartProductService implements CartProductServiceInterface {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final StockServiceInterface stockService;

  @Override
  @Transactional
  public CartItemResponse addProductToCart(User user, ManageCartRequest request) {
    Product product =
        productRepository
            .findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    CartItem cartItem =
        cartItemRepository
            .findByCartIdAndProductId(getUserCart(user).getId(), request.productId())
            .orElse(null);

    int currentQuantityInCart = (cartItem != null) ? cartItem.getQuantity() : 0;
    int requestedQuantity = request.quantity();
    int availableStock = stockService.getCurrentStock(product);

    if (availableStock < (currentQuantityInCart + requestedQuantity)) {
      throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
    }

    if (cartItem != null) {
      cartItem.setQuantity(currentQuantityInCart + requestedQuantity);
    } else {
      cartItem =
          CartItem.builder()
              .cart(getUserCart(user))
              .product(product)
              .quantity(requestedQuantity)
              .build();
    }
    cartItemRepository.save(cartItem);

    return new CartItemResponse(
        cartItem.getProduct().getId(),
        cartItem.getProduct().getName(),
        cartItem.getQuantity(),
        cartItem.getProduct().getPrice());
  }

  @Override
  public void removeProductFromCart(User user, ManageCartRequest request) {
    CartItem cartItem =
        cartItemRepository
            .findByCartIdAndProductId(getUserCart(user).getId(), request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));

    if (request.quantity() >= cartItem.getQuantity()) {
      cartItemRepository.delete(cartItem);
    } else {
      cartItem.setQuantity(cartItem.getQuantity() - request.quantity());
      cartItemRepository.save(cartItem);
    }
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

  public Cart getUserCart(User user) {
    return user.getCart();
  }
}
