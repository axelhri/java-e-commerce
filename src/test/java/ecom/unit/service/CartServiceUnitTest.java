package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ecom.entity.Cart;
import ecom.entity.User;
import ecom.exception.CartAlreadyExistsException;
import ecom.repository.CartRepository;
import ecom.service.CartService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceUnitTest {
  @Mock private CartRepository cartRepository;

  @InjectMocks private CartService cartService;

  @Nested
  class createCartUnitTest {
    @Test
    void createCartShouldCreateCartWhenUserHasNoCart() {
      // Arrange
      User user = new User();
      user.setEmail("test@example.com");
      Cart cart = Cart.builder().user(user).build();
      when(cartRepository.save(any(Cart.class))).thenReturn(cart);

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
      User user = new User();
      user.setEmail("test@example.com");
      Cart existingCart = Cart.builder().user(user).build();
      user.setCart(existingCart);

      // Act & Assert
      CartAlreadyExistsException exception =
          assertThrows(CartAlreadyExistsException.class, () -> cartService.createCart(user));

      assertEquals("test@example.com cart already exists", exception.getMessage());
      verify(cartRepository, never()).save(any(Cart.class));
    }
  }
}
