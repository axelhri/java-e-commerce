package neora.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Bookmarks", description = "Endpoints for managing user bookmarks")
@Slf4j
public class BookmarkController {
  private final BookmarkServiceInterface bookmarkServiceInterface;

  @Operation(
      summary = "Bookmark a product",
      description = "Adds a product to the user's bookmarks.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Product bookmarked successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
        @ApiResponse(
            responseCode = "409",
            description = "Product already bookmarked",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "User not authenticated",
            content = @Content)
      })
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

  @Operation(
      summary = "Remove product from bookmarks",
      description = "Removes a specific product from the user's bookmarks.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Product removed from bookmarks successfully",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "Product or bookmark not found",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "User not authenticated",
            content = @Content)
      })
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

  @Operation(
      summary = "Clear all bookmarks",
      description = "Removes all bookmarks for the authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Bookmarks cleared successfully",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "User not authenticated",
            content = @Content)
      })
  @DeleteMapping("/clear")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void clearBookmarks(@AuthenticationPrincipal User user) {
    log.info("Received request to clear bookmarks for user ID: {}", user.getId());
    bookmarkServiceInterface.clearBookmarks(user);
    log.info("Bookmarks cleared successfully for user ID: {}", user.getId());
  }
}
