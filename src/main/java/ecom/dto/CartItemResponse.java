package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record CartItemResponse(
    @JsonProperty("product_id") UUID productId,
    @JsonProperty("product_name") String productName,
    @JsonProperty("product_quantity") int quantity,
    @JsonProperty("product_price") int price) {}
