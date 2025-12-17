package ecom.interfaces;

import ecom.dto.RatingRequest;
import ecom.dto.RatingResponse;
import ecom.entity.User;

public interface RatingServiceInterface {
  RatingResponse sendProductRating(User user, RatingRequest request);
}
