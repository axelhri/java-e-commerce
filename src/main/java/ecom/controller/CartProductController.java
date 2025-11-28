package ecom.controller;

import ecom.dto.AddToCartRequest;
import ecom.dto.ApiResponse;
import ecom.dto.CartItemResponse;
import ecom.entity.User;
import ecom.interfaces.CartProductServiceInterface;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/cart-items")
public class CartProductController {
  private final CartProductServiceInterface cartProductService;

  @PostMapping
  public ResponseEntity<ApiResponse<CartItemResponse>> addProductToCart(
      @AuthenticationPrincipal User user, @Valid @RequestBody AddToCartRequest dto) {
    CartItemResponse response = cartProductService.addProductToCart(user, dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiResponse<>(
                Instant.now(),
                HttpStatus.CREATED.value(),
                "Product added to cart successfully",
                response));
  }
}
