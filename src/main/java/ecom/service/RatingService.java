package ecom.service;

import ecom.dto.PagedResponse;
import ecom.dto.RatingRequest;
import ecom.dto.RatingResponse;
import ecom.entity.Product;
import ecom.entity.ProductRating;
import ecom.entity.User;
import ecom.exception.ResourceAlreadyExistsException;
import ecom.exception.ResourceNotFoundException;
import ecom.exception.UnauthorizedAccess;
import ecom.interfaces.RatingServiceInterface;
import ecom.mapper.PageMapper;
import ecom.mapper.RatingMapper;
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
  private final RatingMapper ratingMapper;
  private final PageMapper pageMapper;

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
  public Double getProductAverageRating(UUID productId) {
    if (!productRepository.existsById(productId)) {
      throw new ResourceNotFoundException("Product not found");
    }
    return Optional.ofNullable(productRatingRepository.getAverageRatingByProductId(productId))
        .orElse(5.0);
  }
}
