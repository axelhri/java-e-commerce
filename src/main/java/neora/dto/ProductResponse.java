package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

public record ProductResponse(
    @Schema(
            description = "Product unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_id")
        UUID id,
    @Schema(
            description = "Product name",
            example = "Wireless Mouse",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_name")
        String name,
    @Schema(
            description = "Product price in cents",
            example = "2500",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_price")
        Integer price,
    @Schema(
            description = "Product description",
            example = "Ergonomic wireless mouse...",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_description")
        String description,
    @Schema(
            description = "Current stock quantity",
            example = "95",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_stock")
        Integer stock,
    @Schema(description = "List of product images", accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_images")
        List<ProductImageResponse> images,
    @Schema(description = "Vendor information", accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("vendor")
        VendorSummary vendor) {}
