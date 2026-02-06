package neora.mapper;

import neora.dto.BookmarkResponse;
import neora.entity.Bookmark;
import neora.entity.Product;
import neora.entity.User;
import org.springframework.stereotype.Component;

@Component
public class BookmarkMapper {

  public Bookmark toBookmarkEntity(Product product, User user) {
    return Bookmark.builder().user(user).product(product).build();
  }

  public BookmarkResponse toBookmarkResponse(Bookmark bookmark) {
    return new BookmarkResponse(
        bookmark.getId(), bookmark.getProduct().getId(), bookmark.getUser().getId());
  }
}
