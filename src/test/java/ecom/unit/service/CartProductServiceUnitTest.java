package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.dto.CartItemResponse;
import ecom.dto.ManageCartRequest;
import ecom.entity.Cart;
import ecom.entity.CartItem;
import ecom.entity.Product;
import ecom.entity.User;
import ecom.repository.CartItemRepository;
import ecom.repository.ProductRepository;
import ecom.service.CartProductService;
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
  }
}
