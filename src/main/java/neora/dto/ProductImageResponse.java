package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record ProductImageResponse(
    @Schema(
            description = "Image URL",
            example = "http://res.cloudinary.com/...",
            accessMode = Schema.AccessMode.READ_ONLY,
            format = "uri")
        @JsonProperty("product_images")
        String imageUrl,
    @Schema(
            description = "Display order of the image",
            example = "0",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("display_order")
        Integer displayOrder) {}
