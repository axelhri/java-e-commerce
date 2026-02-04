package neora.controller;

import jakarta.validation.Valid;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.ApiRestResponse;
import neora.dto.BookmarkResponse;
import neora.dto.ManageBookmarkRequest;
import neora.entity.User;
import neora.interfaces.BookmarkServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/bookmarks")
@Slf4j
public class BookmarkController {
  private final BookmarkServiceInterface bookmarkServiceInterface;

  @PostMapping
  public ResponseEntity<ApiRestResponse<BookmarkResponse>> bookmarkProduct(
      @Valid @RequestBody ManageBookmarkRequest request, @AuthenticationPrincipal User user) {
    log.info(
        "Received request to bookmark product ID: {} for user ID: {}",
        request.productId(),
        user.getId());
    BookmarkResponse bookmark = bookmarkServiceInterface.bookmarkProduct(request, user);
    log.info(
        "Successfully bookmarked product ID: {} for user ID: {}",
        request.productId(),
        user.getId());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiRestResponse<>(
                Instant.now(),
                HttpStatus.CREATED.value(),
                "Product added to bookmarks successfully",
                bookmark));
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeProductFromBookmarks(
      @Valid @RequestBody ManageBookmarkRequest request, @AuthenticationPrincipal User user) {
    log.info(
        "Received request to remove bookmark for product ID: {} and user ID: {}",
        request.productId(),
        user.getId());
    bookmarkServiceInterface.removeProductFromBookmarks(request, user);
    log.info(
        "Successfully removed bookmark for product ID: {} and user ID: {}",
        request.productId(),
        user.getId());
  }

  @DeleteMapping("/clear")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void clearBookmarks(@AuthenticationPrincipal User user) {
    log.info("Received request to clear bookmarks for user ID: {}", user.getId());
    bookmarkServiceInterface.clearBookmarks(user);
    log.info("Bookmarks cleared successfully for user ID: {}", user.getId());
  }
}
