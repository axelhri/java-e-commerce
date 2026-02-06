package neora.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import neora.dto.BookmarkResponse;
import neora.dto.ManageBookmarkRequest;
import neora.entity.Bookmark;
import neora.entity.Product;
import neora.entity.User;
import neora.exception.ResourceAlreadyExistsException;
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
  private BookmarkResponse bookmarkResponse;

  @BeforeEach
  void setUp() {
    user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
    product = Product.builder().id(UUID.randomUUID()).name("Test Product").build();
    request = new ManageBookmarkRequest(product.getId());
    bookmark = Bookmark.builder().id(UUID.randomUUID()).user(user).product(product).build();
    bookmarkResponse = new BookmarkResponse(bookmark.getId(), product.getId(), user.getId());
  }

  @Nested
  class BookmarkProduct {

    @Test
    void should_bookmark_product_successfully() {
      // Arrange
      when(userRepository.existsById(user.getId())).thenReturn(true);
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(bookmarkRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());
      when(bookmarkMapper.toBookmarkEntity(product, user)).thenReturn(bookmark);
      when(bookmarkRepository.save(bookmark)).thenReturn(bookmark);
      when(bookmarkMapper.toBookmarkResponse(bookmark)).thenReturn(bookmarkResponse);

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

    @Test
    void should_throw_exception_if_product_already_bookmarked() {
      // Arrange
      when(userRepository.existsById(user.getId())).thenReturn(true);
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(bookmarkRepository.findByUserAndProduct(user, product))
          .thenReturn(Optional.of(bookmark));

      // Act & Assert
      ResourceAlreadyExistsException exception =
          assertThrows(
              ResourceAlreadyExistsException.class,
              () -> bookmarkService.bookmarkProduct(request, user));

      assertEquals("Product already bookmarked", exception.getMessage());
      verify(bookmarkRepository, never()).save(any());
    }
  }

  @Nested
  class RemoveProductFromBookmarks {

    @Test
    void should_remove_bookmark_successfully() {
      // Arrange
      when(userRepository.existsById(user.getId())).thenReturn(true);
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(bookmarkRepository.findByUserAndProduct(user, product))
          .thenReturn(Optional.of(bookmark));

      // Act
      bookmarkService.removeProductFromBookmarks(request, user);

      // Assert
      verify(bookmarkRepository).delete(bookmark);
    }

    @Test
    void should_throw_exception_if_user_not_found() {
      // Arrange
      when(userRepository.existsById(user.getId())).thenReturn(false);

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> bookmarkService.removeProductFromBookmarks(request, user));

      assertEquals("User not found", exception.getMessage());
      verify(bookmarkRepository, never()).delete(any());
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
              () -> bookmarkService.removeProductFromBookmarks(request, user));

      assertEquals("Product not found", exception.getMessage());
      verify(bookmarkRepository, never()).delete(any());
    }

    @Test
    void should_throw_exception_if_bookmark_not_found() {
      // Arrange
      when(userRepository.existsById(user.getId())).thenReturn(true);
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(bookmarkRepository.findByUserAndProduct(user, product)).thenReturn(Optional.empty());

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> bookmarkService.removeProductFromBookmarks(request, user));

      assertEquals("Product not found in bookmarks", exception.getMessage());
      verify(bookmarkRepository, never()).delete(any());
    }
  }

  @Nested
  class ClearBookmarks {
    @Test
    void should_clear_bookmarks_successfully() {
      // Act
      bookmarkService.clearBookmarks(user);

      // Assert
      verify(bookmarkRepository).deleteAllByUser(user);
    }
  }

  @Nested
  class GetUserBookmarks {
    @Test
    void should_return_user_bookmarks_successfully() {
      // Arrange
      List<Bookmark> bookmarks = List.of(bookmark);
      when(bookmarkRepository.findAllByUser(user)).thenReturn(bookmarks);
      when(bookmarkMapper.toBookmarkResponse(bookmark)).thenReturn(bookmarkResponse);

      // Act
      List<BookmarkResponse> result = bookmarkService.getUserBookmarks(user);

      // Assert
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(bookmarkResponse, result.get(0));
      verify(bookmarkRepository).findAllByUser(user);
    }

    @Test
    void should_return_empty_list_if_no_bookmarks() {
      // Arrange
      when(bookmarkRepository.findAllByUser(user)).thenReturn(List.of());

      // Act
      List<BookmarkResponse> result = bookmarkService.getUserBookmarks(user);

      // Assert
      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(bookmarkRepository).findAllByUser(user);
    }
  }
}
