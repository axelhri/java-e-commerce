package ecom.controller;

import ecom.dto.*;
import ecom.entity.User;
import ecom.interfaces.RatingServiceInterface;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/ratings")
public class RatingController {
  private RatingServiceInterface ratingService;

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
}
