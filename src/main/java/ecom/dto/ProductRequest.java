package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ProductRequest(
    @JsonProperty("product_name") @NotBlank(message = "Name is required.") @Size(min = 3, max = 255)
        String name,
    @JsonProperty("product_price") @NotNull(message = "Price is required.") @Positive Integer price,
    @JsonProperty("product_description")
        @NotBlank(message = "Description is required.")
        @Size(min = 10)
        String description,
    @JsonProperty("product_stock")
        @Positive(message = "Stock must be a positive number.")
        @NotNull(message = "Stock is required.")
        Integer stock,
    @JsonProperty("vendor") @NotNull(message = "Vendor is required.") UUID vendor,
    @JsonProperty("category") @NotNull(message = "Category is required.") UUID category) {}
