package ecom.controller;

import ecom.dto.ApiResponse;
import ecom.dto.CartItemResponse;
import ecom.entity.User;
import ecom.interfaces.CartServiceInterface;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {
  private final CartServiceInterface cartService;

  @GetMapping("/total")
  public ResponseEntity<Map<String, BigDecimal>> getCartTotalAmount(
      @AuthenticationPrincipal User user) {
    BigDecimal total = cartService.getCartTotalAmount(user);
    return ResponseEntity.ok(Collections.singletonMap("total", total));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCartProducts(
      @AuthenticationPrincipal User user) {
    List<CartItemResponse> cartProducts = cartService.getCartProducts(user);
    return ResponseEntity.ok(
        new ApiResponse<>(
            Instant.now(),
            HttpStatus.OK.value(),
            "Cart products fetched successfully",
            cartProducts));
  }
}
