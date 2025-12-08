package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CancelOrderRequest(
    @JsonProperty("order_id") @NotNull(message = "Order ID must not be null.") UUID orderId) {}
