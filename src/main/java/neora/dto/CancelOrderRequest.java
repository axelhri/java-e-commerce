package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CancelOrderRequest(
    @Schema(
            description = "Order unique identifier to cancel",
            example = "123e4567-e89b-12d3-a456-426614174000")
        @JsonProperty("order_id")
        @NotNull(message = "Order ID must not be null.")
        UUID orderId) {}
