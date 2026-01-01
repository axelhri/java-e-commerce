package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record RatingResponse(
    @Schema(
            description = "Rating unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("rating_id")
        UUID ratingId,
    @Schema(
            description = "Product unique identifier",
            example = "987e6543-e21b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("product_id")
        UUID productId,
    @Schema(
            description = "Rating value (1 to 5 stars)",
            example = "5",
            accessMode = Schema.AccessMode.READ_ONLY,
            minimum = "1",
            maximum = "5")
        @JsonProperty("rating_stars")
        Integer ratingStars) {}
