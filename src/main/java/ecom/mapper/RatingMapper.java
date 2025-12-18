package ecom.mapper;

import ecom.dto.RatingResponse;
import ecom.entity.ProductRating;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {
  public RatingResponse productRatingToRatingResponse(@NonNull ProductRating productRating) {
    return new RatingResponse(
        productRating.getId(),
        productRating.getProduct().getId(),
        productRating.getRatingEnum().getRating());
  }
}
