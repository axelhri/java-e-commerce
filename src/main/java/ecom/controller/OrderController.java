package ecom.controller;

import ecom.dto.ApiResponse;
import ecom.dto.CancelOrderRequest;
import ecom.dto.OrderRequest;
import ecom.dto.OrderResponse;
import ecom.entity.User;
import ecom.interfaces.OrderServiceInterface;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
  private final OrderServiceInterface orderService;

  @PostMapping
  public ResponseEntity<ApiResponse<OrderResponse>> initiateOrder(
      @AuthenticationPrincipal User user, @Valid @RequestBody OrderRequest dto) {
    OrderResponse response = orderService.initiateOrder(user, dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiResponse<>(
                Instant.now(), HttpStatus.CREATED.value(), "Order passed successfully", response));
  }

  @PostMapping("/cancel")
  public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
      @AuthenticationPrincipal User user, @Valid @RequestBody CancelOrderRequest request) {
    OrderResponse response = orderService.cancelOrder(user, request);
    return ResponseEntity.status(HttpStatus.OK)
        .body(
            new ApiResponse<>(
                Instant.now(), HttpStatus.OK.value(), "Order cancelled successfully", response));
  }
}
