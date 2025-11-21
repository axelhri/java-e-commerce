package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ProductRequest(
    @JsonProperty("product_name") @NotBlank @Size(min = 3, max = 100) String name,
    @JsonProperty("price_name") @NotNull @Positive Integer price,
    @JsonProperty("description_name") @NotBlank @Size(max = 100) String description,
    @JsonProperty("vendor") @NotNull UUID vendor,
    @JsonProperty("category") @NotNull UUID category) {}
