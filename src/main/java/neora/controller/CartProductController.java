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
import neora.dto.CartItemResponse;
import neora.dto.ManageCartRequest;
import neora.entity.User;
import neora.interfaces.CartProductServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/cart-items")
@Tag(name = "Cart Items", description = "Endpoints for managing individual items within the cart")
@Slf4j
public class CartProductController {
  private final CartProductServiceInterface cartProductService;

  @Operation(
      summary = "Add product to cart",
      description = "Adds a product to the user's cart or updates quantity if already present.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Product added to cart successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or insufficient stock",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
      })
  @PostMapping
  public ResponseEntity<ApiRestResponse<CartItemResponse>> addProductToCart(
      @AuthenticationPrincipal User user, @Valid @RequestBody ManageCartRequest dto) {
    log.info(
        "Received request to add product {} to cart for user {}", dto.productId(), user.getId());
    CartItemResponse response = cartProductService.addProductToCart(user, dto);
    log.info("Successfully added product {} to cart for user {}", dto.productId(), user.getId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiRestResponse<>(
                Instant.now(),
                HttpStatus.CREATED.value(),
                "Product added to cart successfully",
                response));
  }

  @Operation(
      summary = "Remove product from cart",
      description = "Removes a specific product from the user's cart.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Product removed successfully",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found in cart",
            content = @Content)
      })
  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeProductFromCart(
      @AuthenticationPrincipal User user, @Valid @RequestBody ManageCartRequest dto) {
    log.info(
        "Received request to remove product {} from cart for user {}",
        dto.productId(),
        user.getId());
    cartProductService.removeProductFromCart(user, dto);
    log.info(
        "Successfully removed product {} from cart for user {}", dto.productId(), user.getId());
  }
}
