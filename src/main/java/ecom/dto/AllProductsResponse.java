package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record AllProductsResponse(
    @JsonProperty("product_id") UUID id,
    @JsonProperty("product_name") String name,
    @JsonProperty("product_price") Integer price,
    @JsonProperty("product_slug") String slug,
    @JsonProperty("product_stock") Integer stock,
    @JsonProperty("product_image") String primaryImage,
    @JsonProperty("product_rating") Double rating) {}
