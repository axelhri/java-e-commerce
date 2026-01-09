package neora.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    log.info("Attempting to rate product ID: {} by user ID: {}", request.productId(), user.getId());

    Product product =
        productRepository
            .findById(request.productId())
            .orElseThrow(
                () -> {
                  log.error("Product not found for ID: {}", request.productId());
                  return new ResourceNotFoundException("Product not found");
                });

    boolean hasPurchased =
        orderRepository.existsByUserAndOrderItemsProductAndStatus(user, product, OrderStatus.PAID);
    if (!hasPurchased) {
      log.warn(
          "User ID: {} attempted to rate product ID: {} without purchase",
          user.getId(),
          product.getId());
      throw new UnauthorizedAccess("You can only rate products you have purchased and received.");
    }

    Optional<ProductRating> existingRating =
        productRatingRepository.findByUserAndProduct(user, product);
    if (existingRating.isPresent()) {
      log.warn("User ID: {} has already rated product ID: {}", user.getId(), product.getId());
      throw new ResourceAlreadyExistsException("You have already rated this product.");
    }

    Rating ratingValue = Rating.fromValue(request.ratingStars());

    ProductRating productRating = ProductRating.builder().product(product).user(user).build();

    productRating.setRating(ratingValue);

    ProductRating savedRating = productRatingRepository.save(productRating);
    log.info(
        "Rating saved successfully for product ID: {} by user ID: {}",
        product.getId(),
        user.getId());

    return ratingMapper.productRatingToRatingResponse(savedRating);
  }

  @Override
  public Double getVendorRating(UUID vendorId) {
    log.debug("Fetching average rating for vendor ID: {}", vendorId);
    Double rating =
        Optional.ofNullable(productRatingRepository.getAverageRatingByVendorId(vendorId))
            .orElse(5.0);
    log.debug("Average rating for vendor ID {}: {}", vendorId, rating);
    return rating;
  }

  @Override
  public PagedResponse<RatingResponse> getProductRatings(UUID productId, Pageable pageable) {
    log.info("Fetching ratings for product ID: {}, page: {}", productId, pageable.getPageNumber());
    if (!productRepository.existsById(productId)) {
      log.error("Product not found for ID: {}", productId);
      throw new ResourceNotFoundException("Product not found");
    }

    Page<RatingResponse> page =
        productRatingRepository
            .findByProductId(productId, pageable)
            .map(ratingMapper::productRatingToRatingResponse);

    log.info(
        "Found {} ratings for product ID: {} on page {}",
        page.getNumberOfElements(),
        productId,
        pageable.getPageNumber());
    return pageMapper.toPagedResponse(page);
  }

  @Override
  public Map<UUID, Double> getRatings(List<UUID> productIds) {
    log.debug("Fetching average ratings for {} products", productIds.size());
    Map<UUID, Double> ratings =
        ratingRepository.getAverageRatingForProducts(productIds).stream()
            .collect(
                Collectors.toMap(
                    ProductAverageRating::getProductId, ProductAverageRating::getAverageRating));
    log.debug("Successfully fetched average ratings for {} products", ratings.size());
    return ratings;
  }
}
