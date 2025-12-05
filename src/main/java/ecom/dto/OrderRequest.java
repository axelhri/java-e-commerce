package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record OrderRequest(
    @JsonProperty("cart_items")
        @NotEmpty(message = "You must order at least 1 product.")
        @NotNull(message = "You must order at least 1 product.")
        Set<UUID> productIds) {}
