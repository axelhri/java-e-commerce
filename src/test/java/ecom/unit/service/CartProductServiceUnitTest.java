package ecom.unit.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.dto.CartItemResponse;
import ecom.dto.ManageCartRequest;
import ecom.entity.Cart;
import ecom.entity.CartItem;
import ecom.entity.Product;
import ecom.entity.User;
import ecom.exception.ResourceNotFoundException;
import ecom.repository.CartItemRepository;
import ecom.repository.ProductRepository;
import ecom.service.CartProductService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartProductServiceUnitTest {
  @Mock private CartItemRepository cartItemRepository;
  @Mock private ProductRepository productRepository;
  @Mock private CartProductService cartSelfService;
  @Spy @InjectMocks private CartProductService cartProductService;

  private User user;
  private Cart cart;
  private Product product;
  private ManageCartRequest request;
  private CartItem cartItem;

  @BeforeEach
  void setUp() {
    user = User.builder().email("test@example.com").password("Password123!").build();
    product =
        Product.builder()
            .name("Wireless mouse")
            .price(1000)
            .description("Black wireless mouse.")
            .build();
    cart = Cart.builder().user(user).build();
    request = new ManageCartRequest(product.getId(), 1);
    cartItem = CartItem.builder().product(product).cart(cart).quantity(1).build();
  }

  @Nested
  class addProductToCart {

    @Test
    void should_add_product_to_cart_successfully() {
      // Arrange
      doReturn(cart).when(cartProductService).getUserCart(user);
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
          .thenReturn(Optional.empty());
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
      doReturn(cart).when(cartProductService).getUserCart(user);
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
          .thenReturn(Optional.of(cartItem));

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
      doReturn(cart).when(cartProductService).getUserCart(user);
      when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
          .thenReturn(Optional.of(cartItem));

      // Act
      cartProductService.removeProductFromCart(user, request);

      // Assert
      verify(cartProductService, times(1)).removeProductFromCart(user, request);
    }

    @Test
    void should_remove_product_exact_quantity_if_it_already_exists() {
      // Arrange
      cartItem.setQuantity(5);
      request = new ManageCartRequest(product.getId(), 4);
      doReturn(cart).when(cartProductService).getUserCart(user);
      when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
          .thenReturn(Optional.of(cartItem));

      // Act
      cartProductService.removeProductFromCart(user, request);

      // Assert
      assertEquals(1, cartItem.getQuantity());
      verify(cartProductService, times(1)).removeProductFromCart(user, request);
    }

    @Test
    void should_throw_exception_if_product_is_not_in_cart() {
      // Arrange
      doReturn(cart).when(cartProductService).getUserCart(user);
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

  @Nested
  class getCartTotalAmount {

    @Test
    void should_get_cart_total_amount() {
      // Arrange
      when(cartProductService.getUserCart(user)).thenReturn(cart);

      Product product2 =
          Product.builder().name("product2").price(800).description("random description.").build();

      cartItem.setProduct(product);
      CartItem cartItem2 = CartItem.builder().product(product2).cart(cart).quantity(1).build();

      cartItem2.setProduct(product2);

      List<CartItem> items = List.of(cartItem, cartItem2);

      when(cartItemRepository.findByCartId(cart.getId())).thenReturn(items);

      // Act
      BigDecimal total = cartProductService.getCartTotalAmount(user);

      // Assert
      assertEquals(new BigDecimal("18.00"), total);
    }
  }
}
