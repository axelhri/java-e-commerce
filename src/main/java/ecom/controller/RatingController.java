package ecom.controller;

import ecom.dto.*;
import ecom.entity.User;
import ecom.interfaces.RatingServiceInterface;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/ratings")
public class RatingController {
  private final RatingServiceInterface ratingService;

  @PostMapping("/products")
  public ResponseEntity<ApiResponse<RatingResponse>> sendProductRating(
      @AuthenticationPrincipal User user, @Valid @RequestBody RatingRequest dto) {
    RatingResponse response = ratingService.sendProductRating(user, dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiResponse<>(
                Instant.now(),
                HttpStatus.CREATED.value(),
                "Product rating created successfully",
                response));
  }

  @GetMapping("/vendor/{id}")
  public ResponseEntity<ApiResponse<VendorRatingResponse>> getVendorRating(@PathVariable UUID id) {
    Double averageRating = ratingService.getVendorRating(id);
    VendorRatingResponse response = new VendorRatingResponse(id, averageRating);
    return ResponseEntity.ok(
        new ApiResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Vendor rating fetched successfully", response));
  }

  @GetMapping("product/{id}")
  public ResponseEntity<ApiResponse<PagedResponse<RatingResponse>>> getProductRatings(
      @PathVariable UUID id, Pageable pageable) {
    PagedResponse<RatingResponse> page = ratingService.getProductRatings(id, pageable);

    return ResponseEntity.ok(
        new ApiResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Product ratings fetched successfully", page));
  }
}
