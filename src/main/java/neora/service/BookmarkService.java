package neora.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.BookmarkResponse;
import neora.dto.ManageBookmarkRequest;
import neora.entity.Bookmark;
import neora.entity.Product;
import neora.entity.User;
import neora.exception.ResourceAlreadyExistsException;
import neora.exception.ResourceNotFoundException;
import neora.interfaces.BookmarkServiceInterface;
import neora.mapper.BookmarkMapper;
import neora.repository.BookmarkRepository;
import neora.repository.ProductRepository;
import neora.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class BookmarkService implements BookmarkServiceInterface {
  private final UserRepository userRepository;
  private final BookmarkRepository bookmarkRepository;
  private final BookmarkMapper bookmarkMapper;
  private final ProductRepository productRepository;

  @Override
  public BookmarkResponse bookmarkProduct(ManageBookmarkRequest request, User user) {
    log.info(
        "Attempting to bookmark product ID: {} for user ID: {}", request.productId(), user.getId());

    if (!userRepository.existsById(user.getId())) {
      log.error("User not found for ID: {}", user.getId());
      throw new ResourceNotFoundException("User not found");
    }

    Product product =
        productRepository
            .findById(request.productId())
            .orElseThrow(
                () -> {
                  log.error("Product not found for ID: {}", request.productId());
                  return new ResourceNotFoundException("Product not found");
                });

    if (bookmarkRepository.findByUserAndProduct(user, product).isPresent()) {
      log.warn(
          "Product ID: {} is already bookmarked by user ID: {}", product.getId(), user.getId());
      throw new ResourceAlreadyExistsException("Product already bookmarked");
    }

    Bookmark bookmark = bookmarkMapper.toBookmarkEntity(product, user);

    bookmarkRepository.save(bookmark);
    log.info(
        "Product ID: {} successfully bookmarked for user ID: {}", product.getId(), user.getId());

    return new BookmarkResponse(
        bookmark.getId(), bookmark.getProduct().getId(), bookmark.getUser().getId());
  }

  @Override
  public void removeProductFromBookmarks(ManageBookmarkRequest request, User user) {
    log.info(
        "Attempting to remove bookmark for product ID: {} and user ID: {}",
        request.productId(),
        user.getId());

    if (!userRepository.existsById(user.getId())) {
      log.error("User not found for ID: {}", user.getId());
      throw new ResourceNotFoundException("User not found");
    }

    Product product =
        productRepository
            .findById(request.productId())
            .orElseThrow(
                () -> {
                  log.error("Product not found for ID: {}", request.productId());
                  return new ResourceNotFoundException("Product not found");
                });

    Bookmark bookmark =
        bookmarkRepository
            .findByUserAndProduct(user, product)
            .orElseThrow(
                () -> {
                  log.warn(
                      "Bookmark not found for user ID: {} and product ID: {}",
                      user.getId(),
                      product.getId());
                  return new ResourceNotFoundException("Product not found in bookmarks");
                });

    bookmarkRepository.delete(bookmark);
    log.info(
        "Bookmark removed successfully for product ID: {} and user ID: {}",
        product.getId(),
        user.getId());
  }

  @Override
  @Transactional
  public void clearBookmarks(User user) {
    bookmarkRepository.deleteAllByUser(user);
  }
}
