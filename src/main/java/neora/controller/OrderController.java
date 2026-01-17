package neora.controller;

import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.*;
import neora.entity.User;
import neora.interfaces.OrderServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Endpoints for managing user orders")
@Slf4j
public class OrderController {
  private final OrderServiceInterface orderService;

  @Operation(
      summary = "Initiate an order",
      description = "Creates a new order from the user's cart.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Order created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input or empty cart",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "User not authenticated",
            content = @Content)
      })
  @PostMapping
  public ResponseEntity<ApiRestResponse<PaymentResponse>> initiateOrder(
      @AuthenticationPrincipal User user, @Valid @RequestBody OrderRequest dto)
      throws StripeException {
    log.info("Received request to initiate order for user ID: {}", user.getId());
    PaymentResponse response = orderService.initiateOrder(user, dto);
    log.info("Order initiated successfully for user ID: {}", user.getId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiRestResponse<>(
                Instant.now(), HttpStatus.CREATED.value(), "Order passed successfully", response));
  }

  @Operation(
      summary = "Retry payment for an order",
      description =
          "Retries the payment process for an existing order if the previous attempt failed.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment retry initiated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "User not authorized to access this order",
            content = @Content)
      })
  @PostMapping("/{orderId}/retry-payment")
  public ResponseEntity<ApiRestResponse<PaymentResponse>> retryPayment(
      @AuthenticationPrincipal User user, @PathVariable UUID orderId) throws StripeException {
    log.info(
        "Received request to retry payment for order ID: {} by user ID: {}", orderId, user.getId());
    PaymentResponse response = orderService.retryPayment(user, orderId);
    log.info("Payment retry initiated successfully for order ID: {}", orderId);
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new ApiRestResponse<>(
                Instant.now(),
                HttpStatus.OK.value(),
                "Order passed successfully, please confirm payment",
                response));
  }

  @Operation(
      summary = "Cancel an order",
      description = "Cancels an existing order and processes a refund if applicable.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order cancelled successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "User not authorized to cancel this order",
            content = @Content),
        @ApiResponse(
            responseCode = "409",
            description = "Order cannot be cancelled in its current state",
            content = @Content)
      })
  @PostMapping("/cancel")
  public ResponseEntity<ApiRestResponse<OrderResponse>> cancelOrder(
      @AuthenticationPrincipal User user, @Valid @RequestBody CancelOrderRequest request)
      throws StripeException {
    log.info(
        "Received request to cancel order ID: {} for user ID: {}", request.orderId(), user.getId());
    OrderResponse response = orderService.cancelOrder(user, request);
    log.info("Order cancelled successfully for order ID: {}", request.orderId());
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new ApiRestResponse<>(
                Instant.now(), HttpStatus.OK.value(), "Order cancelled successfully", response));
  }

  @Operation(
      summary = "Get user orders",
      description = "Retrieves all orders placed by the authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Orders fetched successfully",
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
  public ResponseEntity<ApiRestResponse<List<OrderResponse>>> getUserOrders(
      @AuthenticationPrincipal User user) {
    log.info("Received request to get orders for user ID: {}", user.getId());
    List<OrderResponse> orders = orderService.getUserOrders(user);
    log.info("Returning {} orders for user ID: {}", orders.size(), user.getId());
    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Orders fetched successfully", orders));
  }

  @Operation(summary = "Get order by ID", description = "Retrieves details of a specific order.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order fetched successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied or user not authenticated",
            content = @Content)
      })
  @GetMapping("/{orderId}")
  public ResponseEntity<ApiRestResponse<OrderResponse>> getOrderById(
      @AuthenticationPrincipal User user, @PathVariable UUID orderId) {
    log.info("Received request to get order ID: {} for user ID: {}", orderId, user.getId());
    OrderResponse order = orderService.getOrderById(user, orderId);
    log.info("Returning order details for order ID: {}", orderId);
    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Order fetched successfully", order));
  }

  @Operation(
      summary = "Get cancelled orders",
      description = "Retrieves all cancelled orders for the authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cancelled orders fetched successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "User not authenticated",
            content = @Content)
      })
  @GetMapping("/cancelled")
  public ResponseEntity<ApiRestResponse<List<OrderResponse>>> getUserCancelledOrders(
      @AuthenticationPrincipal User user) {
    log.info("Received request to get cancelled orders for user ID: {}", user.getId());
    List<OrderResponse> orders = orderService.getUserCancelledOrders(user);
    log.info("Returning {} cancelled orders for user ID: {}", orders.size(), user.getId());
    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Cancelled orders fetched successfully", orders));
  }

  @Operation(
      summary = "Get products of an order",
      description = "Retrieves the list of products associated with a specific order.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order products fetched successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "User not authorized to access this order",
            content = @Content)
      })
  @GetMapping("/{orderId}/products")
  public ResponseEntity<ApiRestResponse<List<OrderProductResponse>>> getOrderProducts(
      @PathVariable UUID orderId) {
    log.info("Received request to get order products for order ID: {}", orderId);
    List<OrderProductResponse> products = orderService.getOrderProducts(orderId);
    log.info("Returning {} order products for order ID: {}", products.size(), orderId);
    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Order products fetched successfully", products));
  }
}
