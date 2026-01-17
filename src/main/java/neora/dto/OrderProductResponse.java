package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record OrderProductResponse(
    @Schema(
            description = "Product unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_id")
        String productId,
    @Schema(
            description = "Product name",
            example = "Wireless keyboard",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_name")
        String product_name,
    @Schema(
            description = "Product primary image URL",
            example = "http://res.cloudinary.com/...",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_image")
        String image,
    @Schema(
            description = "Product quantity in order",
            example = "3",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("quantity")
        Integer quantity) {}
