package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record OrderRequest(@JsonProperty("cart_items") @NotNull Set<UUID> productIds) {}
