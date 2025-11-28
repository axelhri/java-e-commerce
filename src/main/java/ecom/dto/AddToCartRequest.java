package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.util.UUID;

public record AddToCartRequest(
    @JsonProperty("product_id") @NotNull(message = "Product's id is required") UUID productId,
    @JsonProperty("product_quantity")
        @NotNull(message = "Product quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1 or higher")
        @Positive
        Integer quantity) {}
