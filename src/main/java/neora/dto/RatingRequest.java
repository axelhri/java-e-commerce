package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RatingRequest(
    @Schema(
            description = "Product unique identifier to rate",
            example = "123e4567-e89b-12d3-a456-426614174000")
        @JsonProperty("product_id")
        @NotNull(message = "Product id is required")
        UUID productId,
    @Schema(
            description = "Rating value (1 to 5 stars)",
            example = "5",
            minimum = "1",
            maximum = "5")
        @JsonProperty("product_rating")
        @NotNull(message = "Rating is required")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must be at most 5")
        Integer ratingStars) {}
