package neora.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.*;
import neora.entity.User;
import neora.interfaces.RatingServiceInterface;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/ratings")
@Tag(name = "Ratings", description = "Endpoints for managing product and vendor ratings")
@Slf4j
public class RatingController {
  private final RatingServiceInterface ratingService;

  @Operation(
      summary = "Rate a product",
      description = "Allows an authenticated user to rate a product they have purchased.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Product rating created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or rating value",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "User not authenticated or not allowed to rate (e.g. not purchased)",
            content = @Content),
        @ApiResponse(
            responseCode = "409",
            description = "User has already rated this product",
            content = @Content)
      })
  @PostMapping("/products")
  public ResponseEntity<ApiRestResponse<RatingResponse>> sendProductRating(
      @AuthenticationPrincipal User user, @Valid @RequestBody RatingRequest dto) {
    log.info(
        "Received request to rate product ID: {} by user ID: {}", dto.productId(), user.getId());
    RatingResponse response = ratingService.sendProductRating(user, dto);
    log.info("Successfully created rating for product ID: {}", dto.productId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiRestResponse<>(
                Instant.now(),
                HttpStatus.CREATED.value(),
                "Product rating created successfully",
                response));
  }

  @Operation(
      summary = "Get vendor rating",
      description = "Retrieves the average rating of a vendor.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Vendor rating fetched successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class)))
      })
  @GetMapping("/vendor/{id}")
  public ResponseEntity<ApiRestResponse<VendorRatingResponse>> getVendorRating(
      @Parameter(description = "Vendor unique identifier", required = true) @PathVariable UUID id) {
    log.info("Received request to get average rating for vendor ID: {}", id);
    Double averageRating = ratingService.getVendorRating(id);
    VendorRatingResponse response = new VendorRatingResponse(id, averageRating);
    log.info("Returning average rating {} for vendor ID: {}", averageRating, id);
    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Vendor rating fetched successfully", response));
  }

  @Operation(
      summary = "Get product ratings",
      description = "Retrieves a paginated list of ratings for a specific product.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product ratings fetched successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
      })
  @GetMapping("product/{id}")
  public ResponseEntity<ApiRestResponse<PagedResponse<RatingResponse>>> getProductRatings(
      @Parameter(description = "Product unique identifier", required = true) @PathVariable UUID id,
      @Parameter(description = "Pagination information") Pageable pageable) {
    log.info(
        "Received request to get ratings for product ID: {}, page: {}",
        id,
        pageable.getPageNumber());
    PagedResponse<RatingResponse> page = ratingService.getProductRatings(id, pageable);
    log.info("Returning {} ratings for product ID: {}", page.content().size(), id);
    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Product ratings fetched successfully", page));
  }
}
