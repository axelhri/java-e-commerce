package neora.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import neora.dto.CartItemResponse;
import neora.entity.Cart;
import neora.entity.CartItem;
import neora.entity.Product;
import neora.entity.User;
import neora.exception.CartAlreadyExistsException;
import neora.repository.CartItemRepository;
import neora.repository.CartRepository;
import neora.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {
  @Mock private CartRepository cartRepository;
  @Mock private CartItemRepository cartItemRepository;
  @Spy @InjectMocks private CartService cartService;

  private User user;
  private Cart cart;
  private Product product;
  private CartItem cartItem;

  @BeforeEach
  void setUp() {
    user = User.builder().email("test@example.com").password("Password123!").build();
    product =
        Product.builder()
            .id(UUID.randomUUID())
            .name("Wireless mouse")
            .price(1000)
            .description("Black wireless mouse.")
            .build();
    cart = Cart.builder().id(UUID.randomUUID()).user(user).build();
    user.setCart(cart);
    cartItem = CartItem.builder().product(product).cart(cart).quantity(1).build();
  }

  @Nested
  class createCartUnitTest {
    @Test
    void createCartShouldCreateCartWhenUserHasNoCart() {
      // Arrange
      User user = new User();
      user.setEmail("test@example.com");
      Cart newCart = Cart.builder().user(user).build();
      when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

      // Act
      Cart result = cartService.createCart(user);

      // Assert
      verify(cartRepository, times(1)).save(any(Cart.class));

      assertNotNull(result);
      assertEquals(user, result.getUser());
      assertEquals(user, user.getCart().getUser());
    }

    @Test
    void createCartShouldCreateCartThrowExceptionWhenUserAlreadyHasCart() {

      // Arrange
      User newUser = new User();
      newUser.setEmail("test@example.com");
      Cart existingCart = Cart.builder().user(newUser).build();
      newUser.setCart(existingCart);

      // Act & Assert
      CartAlreadyExistsException exception =
          assertThrows(CartAlreadyExistsException.class, () -> cartService.createCart(newUser));

      assertEquals("test@example.com cart already exists", exception.getMessage());
      verify(cartRepository, never()).save(any(Cart.class));
    }
  }

  @Nested
  class getCartTotalAmount {

    @Test
    void should_get_cart_total_amount() {
      // Arrange
      when(cartService.getUserCart(user)).thenReturn(cart);

      Product product2 =
          Product.builder().name("product2").price(800).description("random description.").build();

      cartItem.setProduct(product);
      CartItem cartItem2 = CartItem.builder().product(product2).cart(cart).quantity(1).build();

      cartItem2.setProduct(product2);

      List<CartItem> items = List.of(cartItem, cartItem2);

      when(cartItemRepository.findByCartId(cart.getId())).thenReturn(items);

      // Act
      BigDecimal total = cartService.getCartTotalAmount(user);

      // Assert
      assertEquals(new BigDecimal("18.00"), total);
    }

    @Test
    void should_not_calculate_empty_cart_total_amount() {
      // Arrange
      doReturn(cart).when(cartService).getUserCart(user);
      when(cartItemRepository.findByCartId(cart.getId())).thenReturn(List.of());

      // Act
      BigDecimal total = cartService.getCartTotalAmount(user);

      // Assert
      assertThat(total).isEqualByComparingTo("0.00");
    }

    @Test
    void should_not_calculate_null_cart_total_amount() {
      // Arrange
      doReturn(cart).when(cartService).getUserCart(user);
      when(cartItemRepository.findByCartId(cart.getId())).thenReturn(null);

      // Act
      BigDecimal total = cartService.getCartTotalAmount(user);

      // Assert
      assertThat(total).isEqualByComparingTo("0.00");
    }
  }

  @Nested
  class GetCartProductsUnitTest {

    @Test
    void should_return_list_of_cart_products_successfully() {
      // Arrange
      Product product2 =
          Product.builder()
              .id(UUID.randomUUID())
              .name("Laptop")
              .description("16 inch blue laptop")
              .price(1500)
              .build();
      CartItem cartItem1 = CartItem.builder().cart(cart).product(product).quantity(1).build();
      CartItem cartItem2 = CartItem.builder().cart(cart).product(product2).quantity(2).build();
      List<CartItem> cartItems = List.of(cartItem1, cartItem2);

      when(cartItemRepository.findByCartId(cart.getId())).thenReturn(cartItems);

      // Act
      List<CartItemResponse> responses = cartService.getCartProducts(user);

      // Assert
      assertNotNull(responses);
      assertEquals(2, responses.size());

      assertEquals(product.getId(), responses.get(0).productId());
      assertEquals(product.getName(), responses.get(0).productName());
      assertEquals(1, responses.get(0).quantity());
      assertEquals(product.getPrice(), responses.get(0).price());

      assertEquals(product2.getId(), responses.get(1).productId());
      assertEquals(product2.getName(), responses.get(1).productName());
      assertEquals(2, responses.get(1).quantity());
      assertEquals(product2.getPrice(), responses.get(1).price());

      verify(cartItemRepository).findByCartId(cart.getId());
    }

    @Test
    void should_return_empty_list_when_cart_is_empty() {
      // Arrange
      when(cartItemRepository.findByCartId(cart.getId())).thenReturn(Collections.emptyList());

      // Act
      List<CartItemResponse> responses = cartService.getCartProducts(user);

      // Assert
      assertNotNull(responses);
      assertTrue(responses.isEmpty());
      verify(cartItemRepository).findByCartId(cart.getId());
    }

    @Test
    void should_return_empty_list_when_cart_items_are_null() {
      // Arrange
      when(cartItemRepository.findByCartId(cart.getId())).thenReturn(null);

      // Act
      List<CartItemResponse> responses = cartService.getCartProducts(user);

      // Assert
      assertNotNull(responses);
      assertTrue(responses.isEmpty());
      verify(cartItemRepository).findByCartId(cart.getId());
    }

    @Test
    void should_ignore_cart_item_if_product_is_null() {
      // Arrange
      CartItem cartItemWithNullProduct =
          CartItem.builder().cart(cart).product(null).quantity(1).build();
      List<CartItem> cartItems = List.of(cartItemWithNullProduct);

      when(cartItemRepository.findByCartId(cart.getId())).thenReturn(cartItems);

      // Act
      List<CartItemResponse> responses = cartService.getCartProducts(user);

      // Assert
      assertNotNull(responses);
      assertTrue(responses.isEmpty());
      verify(cartItemRepository).findByCartId(cart.getId());
    }
  }

  @Nested
  class ClearCart {
    @Test
    void should_clear_cart_successfully() {
      // Act
      cartService.clearCart(user);

      // Assert
      verify(cartItemRepository, times(1)).deleteByCartId(cart.getId());
    }
  }
}
