package ecom.service;

import ecom.dto.RatingRequest;
import ecom.dto.RatingResponse;
import ecom.entity.Product;
import ecom.entity.ProductRating;
import ecom.entity.User;
import ecom.exception.ResourceAlreadyExistsException;
import ecom.exception.ResourceNotFoundException;
import ecom.exception.UnauthorizedAccess;
import ecom.interfaces.RatingServiceInterface;
import ecom.model.OrderStatus;
import ecom.model.Rating;
import ecom.repository.OrderRepository;
import ecom.repository.ProductRatingRepository;
import ecom.repository.ProductRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
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

    return new RatingResponse(
        productRating.getId(),
        productRating.getProduct().getId(),
        productRating.getRatingEnum().getRating());
  }

  @Override
  public Double getVendorRating(UUID vendorId) {
    return Optional.ofNullable(productRatingRepository.getAverageRatingByVendorId(vendorId))
        .orElse(0.0);
  }

  @Override
  public Page<RatingResponse> getProductRatings(UUID productId, Pageable pageable) {
    Page<ProductRating> ratings = productRatingRepository.findByProductId(productId, pageable);
    return ratings.map(
        rating ->
            new RatingResponse(
                rating.getId(), rating.getProduct().getId(), rating.getRatingEnum().getRating()));
  }
}
