package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record OrderResponse(
    @JsonProperty("ordered_items") Set<UUID> productsIds,
    @JsonProperty("order_price") BigDecimal price) {}
