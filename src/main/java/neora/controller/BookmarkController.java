package neora.controller;

import jakarta.validation.Valid;
import java.time.Instant;
import lombok.AllArgsConstructor;
import neora.dto.ApiRestResponse;
import neora.dto.BookmarkResponse;
import neora.dto.ManageBookmarkRequest;
import neora.entity.User;
import neora.interfaces.BookmarkServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {
  private final BookmarkServiceInterface bookmarkServiceInterface;

  @PostMapping
  public ResponseEntity<ApiRestResponse<BookmarkResponse>> bookmarkProduct(
      @Valid @RequestBody ManageBookmarkRequest request, @AuthenticationPrincipal User user) {
    BookmarkResponse bookmark = bookmarkServiceInterface.bookmarkProduct(request, user);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiRestResponse<>(
                Instant.now(),
                HttpStatus.CREATED.value(),
                "Product added to bookmarks successfully",
                bookmark));
  }
}
