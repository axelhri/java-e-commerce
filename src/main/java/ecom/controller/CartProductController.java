package ecom.controller;

import ecom.dto.ApiResponse;
import ecom.dto.CartItemResponse;
import ecom.dto.ManageCartRequest;
import ecom.entity.User;
import ecom.interfaces.CartProductServiceInterface;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/cart-items")
public class CartProductController {
  private final CartProductServiceInterface cartProductService;

  @PostMapping
  public ResponseEntity<ApiResponse<CartItemResponse>> addProductToCart(
      @AuthenticationPrincipal User user, @Valid @RequestBody ManageCartRequest dto) {
    CartItemResponse response = cartProductService.addProductToCart(user, dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiResponse<>(
                Instant.now(),
                HttpStatus.CREATED.value(),
                "Product added to cart successfully",
                response));
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeProductFromCart(
      @AuthenticationPrincipal User user, @Valid @RequestBody ManageCartRequest dto) {
    cartProductService.removeProductFromCart(user, dto);
  }

  @GetMapping("/total")
  public ResponseEntity<Map<String, BigDecimal>> getCartTotalAmount(
      @AuthenticationPrincipal User user) {
    BigDecimal total = cartProductService.getCartTotalAmount(user);
    return ResponseEntity.ok(Collections.singletonMap("total", total));
  }
}
