package neora.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.CartItemResponse;
import neora.dto.ManageCartRequest;
import neora.entity.CartItem;
import neora.entity.Product;
import neora.entity.User;
import neora.exception.InsufficientStockException;
import neora.exception.ResourceNotFoundException;
import neora.interfaces.CartProductServiceInterface;
import neora.interfaces.CartServiceInterface;
import neora.interfaces.StockServiceInterface;
import neora.repository.CartItemRepository;
import neora.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class CartProductService implements CartProductServiceInterface {

  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final StockServiceInterface stockService;
  private final CartServiceInterface cartService;

  @Override
  @Transactional
  public CartItemResponse addProductToCart(User user, ManageCartRequest request) {
    log.info("Attempting to add product {} to cart for user {}", request.productId(), user.getId());

    Product product =
        productRepository
            .findById(request.productId())
            .orElseThrow(
                () -> {
                  log.error("Product not found with ID: {}", request.productId());
                  return new ResourceNotFoundException("Product not found");
                });

    CartItem cartItem =
        cartItemRepository
            .findByCartIdAndProductId(cartService.getUserCart(user).getId(), request.productId())
            .orElse(null);

    int currentQuantityInCart = (cartItem != null) ? cartItem.getQuantity() : 0;
    int requestedQuantity = request.quantity();
    int availableStock = stockService.getCurrentStock(product);

    log.debug(
        "Stock check for product {}: available={}, inCart={}, requested={}",
        product.getId(),
        availableStock,
        currentQuantityInCart,
        requestedQuantity);

    if (availableStock < (currentQuantityInCart + requestedQuantity)) {
      log.warn(
          "Insufficient stock for product {}. Available: {}, Requested total: {}",
          product.getName(),
          availableStock,
          currentQuantityInCart + requestedQuantity);
      throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
    }

    if (cartItem != null) {
      cartItem.setQuantity(currentQuantityInCart + requestedQuantity);
      log.debug("Updated quantity for existing cart item: {}", cartItem.getId());
    } else {
      cartItem =
          CartItem.builder()
              .cart(cartService.getUserCart(user))
              .product(product)
              .quantity(requestedQuantity)
              .build();
      log.debug("Created new cart item for product: {}", product.getId());
    }
    cartItemRepository.save(cartItem);
    log.info(
        "Product {} added to cart successfully. New quantity: {}",
        product.getId(),
        cartItem.getQuantity());

    return new CartItemResponse(
        cartItem.getId(),
        cartItem.getProduct().getId(),
        cartItem.getProduct().getName(),
        cartItem.getProduct().getPrimaryImage().getImageUrl(),
        cartItem.getQuantity(),
        cartItem.getProduct().getPrice());
  }

  @Override
  public void removeProductFromCart(User user, ManageCartRequest request) {
    log.info(
        "Attempting to remove product {} from cart for user {}", request.productId(), user.getId());

    CartItem cartItem =
        cartItemRepository
            .findByCartIdAndProductId(cartService.getUserCart(user).getId(), request.productId())
            .orElseThrow(
                () -> {
                  log.error(
                      "Product {} not found in cart for user {}",
                      request.productId(),
                      user.getId());
                  return new ResourceNotFoundException("Product not found in cart");
                });

    if (request.quantity() >= cartItem.getQuantity()) {
      cartItemRepository.delete(cartItem);
      log.info("Removed cart item {} completely from cart", cartItem.getId());
    } else {
      int newQuantity = cartItem.getQuantity() - request.quantity();
      cartItem.setQuantity(newQuantity);
      cartItemRepository.save(cartItem);
      log.info(
          "Decreased quantity for cart item {}. New quantity: {}", cartItem.getId(), newQuantity);
    }
  }
}
