package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record CartItemResponse(
    @Schema(
            description = "Product unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_id")
        UUID productId,
    @Schema(
            description = "Product name",
            example = "Wireless Mouse",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_name")
        String productName,
    @Schema(
            description = "Quantity in cart",
            example = "2",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_quantity")
        int quantity,
    @Schema(
            description = "Unit price in cents",
            example = "2500",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_price")
        int price) {}
