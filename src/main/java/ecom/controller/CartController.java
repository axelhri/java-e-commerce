package ecom.controller;

import ecom.dto.ApiRestResponse;
import ecom.dto.CartItemResponse;
import ecom.entity.User;
import ecom.interfaces.CartServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Cart", description = "Endpoints for managing the user's shopping cart")
public class CartController {
  private final CartServiceInterface cartService;

  @Operation(
      summary = "Get cart total amount",
      description = "Calculates the total price of all items in the user's cart.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Total amount retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{\"total\": 150.50}"))),
        @ApiResponse(
            responseCode = "403",
            description = "User not authenticated",
            content = @Content)
      })
  @GetMapping("/total")
  public ResponseEntity<Map<String, BigDecimal>> getCartTotalAmount(
      @AuthenticationPrincipal User user) {
    BigDecimal total = cartService.getCartTotalAmount(user);
    return ResponseEntity.ok(Collections.singletonMap("total", total));
  }

  @Operation(
      summary = "Get cart products",
      description = "Retrieves all products currently in the user's cart.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cart products retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "User not authenticated",
            content = @Content)
      })
  @GetMapping
  public ResponseEntity<ApiRestResponse<List<CartItemResponse>>> getCartProducts(
      @AuthenticationPrincipal User user) {
    List<CartItemResponse> cartProducts = cartService.getCartProducts(user);
    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(),
            HttpStatus.OK.value(),
            "Cart products fetched successfully",
            cartProducts));
  }

  @Operation(summary = "Clear cart", description = "Removes all items from the user's cart.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Cart cleared successfully",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "User not authenticated",
            content = @Content)
      })
  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void clearCart(@AuthenticationPrincipal User user) {
    cartService.clearCart(user);
  }
}
