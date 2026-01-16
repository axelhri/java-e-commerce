package neora.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import neora.dto.CartItemResponse;
import neora.dto.ManageCartRequest;
import neora.entity.*;
import neora.exception.ResourceNotFoundException;
import neora.interfaces.CartServiceInterface;
import neora.interfaces.StockServiceInterface;
import neora.repository.CartItemRepository;
import neora.repository.ProductRepository;
import neora.service.CartProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartProductServiceUnitTest {
  @Mock private CartItemRepository cartItemRepository;
  @Mock private ProductRepository productRepository;
  @Mock private StockServiceInterface stockService;
  @Mock private CartServiceInterface cartService;
  @InjectMocks private CartProductService cartProductService;

  private User user;
  private Cart cart;
  private Product product;
  private ManageCartRequest request;
  private CartItem cartItem;

  @BeforeEach
  void setUp() {
    user = User.builder().email("test@example.com").password("Password123!").build();

    ProductImage image = new ProductImage();
    image.setImageUrl("http://image.jpg");

    product =
        Product.builder()
            .id(UUID.randomUUID())
            .name("Wireless mouse")
            .price(1000)
            .description("Black wireless mouse.")
            .build();
    product.setImages(Collections.singletonList(image));

    cart = Cart.builder().id(UUID.randomUUID()).user(user).build();
    user.setCart(cart);

    request = new ManageCartRequest(product.getId(), 1);
    cartItem = CartItem.builder().product(product).cart(cart).quantity(1).build();
  }

  @Nested
  class addProductToCart {

    @Test
    void should_add_product_to_cart_successfully() {
      // Arrange
      when(cartService.getUserCart(user)).thenReturn(cart);
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
          .thenReturn(Optional.empty());
      when(stockService.getCurrentStock(product)).thenReturn(100);
      when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

      // Act
      CartItemResponse response = cartProductService.addProductToCart(user, request);

      // Assert
      assertNotNull(response);
      assertEquals(response.productName(), product.getName());
      assertEquals(response.productId(), product.getId());
      assertEquals(response.price(), product.getPrice());

      verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    void should_increase_product_quantity_if_it_already_exists() {
      // Arrange
      when(cartService.getUserCart(user)).thenReturn(cart);
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
          .thenReturn(Optional.of(cartItem));
      when(stockService.getCurrentStock(product)).thenReturn(100);

      // Act
      CartItemResponse response = cartProductService.addProductToCart(user, request);

      // Assert
      assertEquals(response.productId(), product.getId());
      assertEquals(response.quantity(), cartItem.getQuantity());
    }

    @Test
    void should_throw_exception_if_product_does_not_exist() {
      // Arrange
      when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> cartProductService.addProductToCart(user, request));

      assertEquals("Product not found", exception.getMessage());
    }
  }

  @Nested
  class removeProductFromCart {
    @Test
    void should_remove_product_from_cart_successfully() {
      // Arrange
      when(cartService.getUserCart(user)).thenReturn(cart);
      when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
          .thenReturn(Optional.of(cartItem));

      // Act
      cartProductService.removeProductFromCart(user, request);

      // Assert
      verify(cartItemRepository, times(1)).delete(cartItem);
      verify(cartItemRepository, never()).save(any());
    }

    @Test
    void should_remove_product_exact_quantity_if_it_already_exists() {
      // Arrange
      cartItem.setQuantity(5);
      request = new ManageCartRequest(product.getId(), 4);

      when(cartService.getUserCart(user)).thenReturn(cart);
      when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
          .thenReturn(Optional.of(cartItem));

      // Act
      cartProductService.removeProductFromCart(user, request);

      // Assert
      assertEquals(1, cartItem.getQuantity());
      verify(cartItemRepository, times(1)).save(cartItem);
      verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void should_throw_exception_if_product_is_not_in_cart() {
      // Arrange
      when(cartService.getUserCart(user)).thenReturn(cart);
      when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
          .thenReturn(Optional.empty());

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> cartProductService.removeProductFromCart(user, request));

      assertEquals("Product not found in cart", exception.getMessage());
    }
  }
}
