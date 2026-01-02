package neora.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import neora.dto.PagedResponse;
import neora.dto.ProductAverageRating;
import neora.dto.RatingRequest;
import neora.dto.RatingResponse;
import neora.entity.Product;
import neora.entity.ProductRating;
import neora.entity.User;
import neora.exception.ResourceAlreadyExistsException;
import neora.exception.ResourceNotFoundException;
import neora.exception.UnauthorizedAccess;
import neora.interfaces.RatingServiceInterface;
import neora.mapper.PageMapper;
import neora.mapper.RatingMapper;
import neora.model.OrderStatus;
import neora.model.Rating;
import neora.repository.OrderRepository;
import neora.repository.ProductRatingRepository;
import neora.repository.ProductRepository;
import neora.repository.RatingRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class RatingService implements RatingServiceInterface {
  private final ProductRepository productRepository;
  private final ProductRatingRepository productRatingRepository;
  private final OrderRepository orderRepository;
  private final RatingMapper ratingMapper;
  private final PageMapper pageMapper;
  private final RatingRepository ratingRepository;

  @Override
  @Transactional
  public RatingResponse sendProductRating(User user, RatingRequest request) {
    Product product =
        productRepository
            .findById(request.productId())
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

    boolean hasPurchased =
        orderRepository.existsByUserAndOrderItemsProductAndStatus(
            user, product, OrderStatus.DELIVERED);
    if (!hasPurchased) {
      throw new UnauthorizedAccess("You can only rate products you have purchased and received.");
    }

    Optional<ProductRating> existingRating =
        productRatingRepository.findByUserAndProduct(user, product);
    if (existingRating.isPresent()) {
      throw new ResourceAlreadyExistsException("You have already rated this product.");
    }

    Rating ratingValue = Rating.fromValue(request.ratingStars());

    ProductRating productRating = ProductRating.builder().product(product).user(user).build();

    productRating.setRating(ratingValue);

    productRatingRepository.save(productRating);

    return ratingMapper.productRatingToRatingResponse(productRating);
  }

  @Override
  public Double getVendorRating(UUID vendorId) {
    return Optional.ofNullable(productRatingRepository.getAverageRatingByVendorId(vendorId))
        .orElse(5.0);
  }

  @Override
  public PagedResponse<RatingResponse> getProductRatings(UUID productId, Pageable pageable) {
    if (!productRepository.existsById(productId)) {
      throw new ResourceNotFoundException("Product not found");
    }

    Page<RatingResponse> page =
        productRatingRepository
            .findByProductId(productId, pageable)
            .map(ratingMapper::productRatingToRatingResponse);
    return pageMapper.toPagedResponse(page);
  }

  @Override
  public Map<UUID, Double> getRatings(List<UUID> productIds) {
    return ratingRepository.getAverageRatingForProducts(productIds).stream()
        .collect(
            Collectors.toMap(
                ProductAverageRating::getProductId, ProductAverageRating::getAverageRating));
  }
}
