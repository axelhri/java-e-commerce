package neora.controller;

import neora.dto.ApiRestResponse;
import neora.dto.CancelOrderRequest;
import neora.dto.OrderRequest;
import neora.dto.OrderResponse;
import neora.entity.User;
import neora.interfaces.OrderServiceInterface;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Endpoints for managing user orders")
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
  public ResponseEntity<ApiRestResponse<OrderResponse>> initiateOrder(
      @AuthenticationPrincipal User user, @Valid @RequestBody OrderRequest dto) {
    OrderResponse response = orderService.initiateOrder(user, dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiRestResponse<>(
                Instant.now(), HttpStatus.CREATED.value(), "Order passed successfully", response));
  }

  @Operation(summary = "Cancel an order", description = "Cancels an existing order.")
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
            description = "Access denied or user not authenticated",
            content = @Content)
      })
  @PostMapping("/cancel")
  public ResponseEntity<ApiRestResponse<OrderResponse>> cancelOrder(
      @AuthenticationPrincipal User user, @Valid @RequestBody CancelOrderRequest request) {
    OrderResponse response = orderService.cancelOrder(user, request);
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
    List<OrderResponse> orders = orderService.getUserOrders(user);
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
    OrderResponse order = orderService.getOrderById(user, orderId);
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
    List<OrderResponse> orders = orderService.getUserCancelledOrders(user);
    return ResponseEntity.ok(
        new ApiRestResponse<>(
            Instant.now(), HttpStatus.OK.value(), "Cancelled orders fetched successfully", orders));
  }
}
