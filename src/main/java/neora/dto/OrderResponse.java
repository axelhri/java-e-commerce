package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record OrderResponse(
    @Schema(
            description = "Order unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("order_id")
        UUID order_id,
    @Schema(
            description = "Set of ordered product IDs",
            example = "[\"123e4567-e89b-12d3-a456-426614174000\"]",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("ordered_items")
        Set<UUID> productsIds,
    @Schema(
            description = "Total price of the order",
            example = "150.50",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("order_price")
        BigDecimal price,
    @JsonProperty("shipping_address_id") UUID shippingAddress) {}
