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
@Tag(name = "Shipping Addresses", description = "Endpoints for managing user shipping addresses")
public class ShippingAddressController {
  private final ShippingAddressServiceInterface shippingAddressService;

  @Operation(
      summary = "Create a shipping address",
      description = "Creates a new shipping address for the authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Shipping address created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
        @ApiResponse(
            responseCode = "409",
            description = "User already has a shipping address",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "User not authenticated",
            content = @Content)
      })
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
