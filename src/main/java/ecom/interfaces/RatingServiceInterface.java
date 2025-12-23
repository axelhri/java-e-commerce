package ecom.interfaces;

import ecom.dto.PagedResponse;
import ecom.dto.RatingRequest;
import ecom.dto.RatingResponse;
import ecom.entity.User;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface RatingServiceInterface {
  RatingResponse sendProductRating(User user, RatingRequest request);

  Double getVendorRating(UUID vendorId);

  PagedResponse<RatingResponse> getProductRatings(UUID productId, Pageable pageable);

  Double getProductAverageRating(UUID productId);
}
