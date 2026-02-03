package neora.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import neora.dto.BookmarkResponse;
import neora.dto.ManageBookmarkRequest;
import neora.entity.Bookmark;
import neora.entity.Product;
import neora.entity.User;
import neora.exception.ResourceNotFoundException;
import neora.mapper.BookmarkMapper;
import neora.repository.BookmarkRepository;
import neora.repository.ProductRepository;
import neora.repository.UserRepository;
import neora.service.BookmarkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceUnitTest {

  @Mock private UserRepository userRepository;
  @Mock private BookmarkRepository bookmarkRepository;
  @Mock private BookmarkMapper bookmarkMapper;
  @Mock private ProductRepository productRepository;
  @InjectMocks private BookmarkService bookmarkService;

  private User user;
  private Product product;
  private ManageBookmarkRequest request;
  private Bookmark bookmark;

  @BeforeEach
  void setUp() {
    user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
    product = Product.builder().id(UUID.randomUUID()).name("Test Product").build();
    request = new ManageBookmarkRequest(product.getId());
    bookmark = Bookmark.builder().id(UUID.randomUUID()).user(user).product(product).build();
  }

  @Nested
  class BookmarkProduct {

    @Test
    void should_bookmark_product_successfully() {
      // Arrange
      when(userRepository.existsById(user.getId())).thenReturn(true);
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(bookmarkMapper.toBookmarkEntity(product, user)).thenReturn(bookmark);
      when(bookmarkRepository.save(bookmark)).thenReturn(bookmark);

      // Act
      BookmarkResponse response = bookmarkService.bookmarkProduct(request, user);

      // Assert
      assertNotNull(response);
      assertEquals(bookmark.getId(), response.bookmarkId());
      assertEquals(product.getId(), response.productId());
      assertEquals(user.getId(), response.userId());
      verify(bookmarkRepository).save(bookmark);
    }

    @Test
    void should_throw_exception_if_user_not_found() {
      // Arrange
      when(userRepository.existsById(user.getId())).thenReturn(false);

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> bookmarkService.bookmarkProduct(request, user));

      assertEquals("User not found", exception.getMessage());
      verify(bookmarkRepository, never()).save(any());
    }

    @Test
    void should_throw_exception_if_product_not_found() {
      // Arrange
      when(userRepository.existsById(user.getId())).thenReturn(true);
      when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> bookmarkService.bookmarkProduct(request, user));

      assertEquals("Product not found", exception.getMessage());
      verify(bookmarkRepository, never()).save(any());
    }
  }
}
