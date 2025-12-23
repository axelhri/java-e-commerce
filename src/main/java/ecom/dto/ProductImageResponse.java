package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ProductImageResponse(
    @JsonProperty("product_images") String imageUrl,
    @JsonProperty("display_order") Integer displayOrder) {}
