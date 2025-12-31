package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record AllProductsResponse(
    @Schema(
            description = "Product unique id",
            example = "02534fae-bd0a-45c8-a523-b6f030bcf480",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_id")
        UUID id,
    @Schema(
            description = "Product name",
            example =
                "YAMEE grosse echarpe femme Hiver Elégant Châle Chaud Carreaux 65 * 180CM Foulard Long Cape Réversible",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_name")
        String name,
    @Schema(
            description = "Product price in cents",
            example = "1690",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_price")
        Integer price,
    @Schema(
            description = "Product unique slug",
            example = "yamee-grosse-echarpe-femme-hiver-elegant",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_slug")
        String slug,
    @Schema(
            description = "Product stock available",
            example = "143",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_stock")
        Integer stock,
    @Schema(
            description = "Product primary image URL",
            example = "http://res.cloudinary.com/...",
            accessMode = Schema.AccessMode.READ_ONLY,
            format = "uri")
        @JsonProperty("product_image")
        String primaryImage,
    @Schema(
            description = "Product average rating (0.0 to 5.0)",
            example = "4.7",
            accessMode = Schema.AccessMode.READ_ONLY,
            minimum = "0",
            maximum = "5")
        @JsonProperty("product_rating")
        Double rating) {}
