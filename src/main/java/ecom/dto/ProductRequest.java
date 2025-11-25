package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ProductRequest(
    @JsonProperty("product_name")
        @NotBlank(message = "Description is required.")
        @Size(min = 3, max = 100)
        String name,
    @JsonProperty("price_name") @NotNull(message = "Price is required.") @Positive Integer price,
    @JsonProperty("description_name")
        @NotBlank(message = "Description is required.")
        @Size(max = 100)
        String description,
    @JsonProperty("vendor") @NotNull(message = "Vendor is required.") UUID vendor,
    @JsonProperty("category") @NotNull(message = "At least one category is required.")
        UUID category) {}
