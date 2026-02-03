package neora.service;

import lombok.AllArgsConstructor;
import neora.dto.BookmarkResponse;
import neora.dto.ManageBookmarkRequest;
import neora.entity.Bookmark;
import neora.entity.Product;
import neora.entity.User;
import neora.exception.ResourceNotFoundException;
import neora.interfaces.BookmarkServiceInterface;
import neora.mapper.BookmarkMapper;
import neora.repository.BookmarkRepository;
import neora.repository.ProductRepository;
import neora.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookmarkService implements BookmarkServiceInterface {
  private final UserRepository userRepository;
  private final BookmarkRepository bookmarkRepository;
  private final BookmarkMapper bookmarkMapper;
  private final ProductRepository productRepository;

  @Override
  public BookmarkResponse bookmarkProduct(ManageBookmarkRequest request, User user) {
    if (!userRepository.existsById(user.getId())) {
      throw new ResourceNotFoundException("User not found");
    }

    Product product =
        productRepository
            .findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    Bookmark bookmark = bookmarkMapper.toBookmarkEntity(product, user);

    bookmarkRepository.save(bookmark);

    return new BookmarkResponse(
        bookmark.getId(), bookmark.getProduct().getId(), bookmark.getUser().getId());
  }
}
