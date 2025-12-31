package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ProductRequest(
    @Schema(
            description = "Product name",
            example = "Wireless Mouse",
            minLength = 3,
            maxLength = 255)
        @JsonProperty("product_name")
        @NotBlank(message = "Name is required.")
        @Size(min = 3, max = 255)
        String name,
    @Schema(description = "Product price in cents", example = "2500", minimum = "1")
        @JsonProperty("product_price")
        @NotNull(message = "Price is required.")
        @Positive
        Integer price,
    @Schema(
            description = "Detailed product description",
            example = "Ergonomic wireless mouse with long battery life...",
            minLength = 10)
        @JsonProperty("product_description")
        @NotBlank(message = "Description is required.")
        @Size(min = 10)
        String description,
    @Schema(description = "Initial stock quantity", example = "100", minimum = "1")
        @JsonProperty("product_stock")
        @Positive(message = "Stock must be a positive number.")
        @NotNull(message = "Stock is required.")
        Integer stock,
    @Schema(
            description = "Vendor unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000")
        @JsonProperty("vendor")
        @NotNull(message = "Vendor is required.")
        UUID vendor,
    @Schema(
            description = "Category unique identifier",
            example = "987e6543-e21b-12d3-a456-426614174000")
        @JsonProperty("category")
        @NotNull(message = "Category is required.")
        UUID category) {}
