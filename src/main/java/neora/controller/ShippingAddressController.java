package neora.controller;

import jakarta.validation.Valid;
import java.time.Instant;
import lombok.AllArgsConstructor;
import neora.dto.ApiRestResponse;
import neora.dto.ShippingAddressRequest;
import neora.dto.ShippingAddressResponse;
import neora.entity.User;
import neora.interfaces.ShippingAddressServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/shipping-addresses")
public class ShippingAddressController {
  private final ShippingAddressServiceInterface shippingAddressService;

  @PostMapping
  public ResponseEntity<ApiRestResponse<ShippingAddressResponse>> createShippingAddress(
      @AuthenticationPrincipal User user, @Valid @RequestBody ShippingAddressRequest request) {
    ShippingAddressResponse response = shippingAddressService.createShippingAddress(user, request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiRestResponse<>(
                Instant.now(),
                HttpStatus.CREATED.value(),
                "Shipping address created successfully",
                response));
  }
}
