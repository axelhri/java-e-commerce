package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.UUID;

public record ManageCartRequest(
    @Schema(
            description = "Product unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000")
        @JsonProperty("product_id")
        @NotNull(message = "Product's id is required")
        UUID productId,
    @Schema(description = "Quantity to add/update", example = "2", minimum = "1")
        @JsonProperty("product_quantity")
        @NotNull(message = "Product quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1 or higher")
        Integer quantity) {}
