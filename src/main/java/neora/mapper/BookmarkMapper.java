package neora.mapper;

import neora.entity.Bookmark;
import neora.entity.Product;
import neora.entity.User;
import org.springframework.stereotype.Component;

@Component
public class BookmarkMapper {

  public Bookmark toBookmarkEntity(Product product, User user) {
    return Bookmark.builder().user(user).product(product).build();
  }
}
