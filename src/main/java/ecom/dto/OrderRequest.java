package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record OrderRequest(
    @Schema(
            description = "Set of product IDs to order (from cart)",
            example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
        @JsonProperty("cart_items")
        @NotEmpty(message = "You must order at least 1 product.")
        @NotNull(message = "You must order at least 1 product.")
        Set<UUID> productIds) {}
