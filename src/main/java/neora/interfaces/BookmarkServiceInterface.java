package neora.interfaces;

import java.util.List;
import neora.dto.BookmarkResponse;
import neora.dto.ManageBookmarkRequest;
import neora.entity.User;

public interface BookmarkServiceInterface {
  BookmarkResponse bookmarkProduct(ManageBookmarkRequest request, User user);

  void removeProductFromBookmarks(ManageBookmarkRequest request, User user);

  void clearBookmarks(User user);

  List<BookmarkResponse> getUserBookmarks(User user);
}
