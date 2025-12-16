package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record ProductResponse(
    @JsonProperty("product_id") UUID id,
    @JsonProperty("product_name") String name,
    @JsonProperty("product_price") Integer price,
    @JsonProperty("product_description") String description,
    @JsonProperty("product_stock") Integer stock) {}
