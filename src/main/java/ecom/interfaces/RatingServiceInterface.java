package ecom.interfaces;

import ecom.dto.RatingRequest;
import ecom.dto.RatingResponse;
import ecom.entity.User;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RatingServiceInterface {
  RatingResponse sendProductRating(User user, RatingRequest request);

  Double getVendorRating(UUID vendorId);

  Page<RatingResponse> getProductRatings(UUID productId, Pageable pageable);
}
