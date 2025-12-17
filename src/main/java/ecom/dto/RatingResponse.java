package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record RatingResponse(
    @JsonProperty("rating_id") UUID ratingId,
    @JsonProperty("product_id") UUID productId,
    @JsonProperty("rating_stars") Integer ratingStars) {}
