package neora.interfaces;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import neora.dto.PagedResponse;
import neora.dto.RatingRequest;
import neora.dto.RatingResponse;
import neora.entity.User;
import org.springframework.data.domain.Pageable;

public interface RatingServiceInterface {
  RatingResponse sendProductRating(User user, RatingRequest request);

  Double getVendorRating(UUID vendorId);

  PagedResponse<RatingResponse> getProductRatings(UUID productId, Pageable pageable);

  Map<UUID, Double> getRatings(List<UUID> productIds);
}
