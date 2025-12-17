package ecom.interfaces;

import ecom.dto.RatingRequest;
import ecom.dto.RatingResponse;
import ecom.entity.User;
import java.util.UUID;

public interface RatingServiceInterface {
  RatingResponse sendProductRating(User user, RatingRequest request);

  Double getVendorRating(UUID vendorId);
}
