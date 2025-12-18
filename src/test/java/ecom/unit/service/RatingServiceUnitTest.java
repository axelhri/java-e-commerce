package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.dto.RatingRequest;
import ecom.dto.RatingResponse;
import ecom.entity.Product;
import ecom.entity.ProductRating;
import ecom.entity.User;
import ecom.exception.ResourceAlreadyExistsException;
import ecom.exception.ResourceNotFoundException;
import ecom.exception.UnauthorizedAccess;
import ecom.mapper.PageMapper;
import ecom.mapper.RatingMapper;
import ecom.model.OrderStatus;
import ecom.repository.OrderRepository;
import ecom.repository.ProductRatingRepository;
import ecom.repository.ProductRepository;
import ecom.service.RatingService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RatingServiceUnitTest {

  @Mock private ProductRepository productRepository;
  @Mock private ProductRatingRepository productRatingRepository;
  @Mock private OrderRepository orderRepository;
  @Mock private PageMapper pageMapper;
  @Mock private RatingMapper ratingMapper;
  @InjectMocks private RatingService ratingService;

  private User user;
  private Product product;
  private RatingRequest ratingRequest;

  @BeforeEach
  void setUp() {
    user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
    product = Product.builder().id(UUID.randomUUID()).name("Test Product").build();
    ratingRequest = new RatingRequest(product.getId(), 5);
  }

  @Nested
  class SendProductRating {

    @Test
    void should_save_rating_successfully() {
      // Arrange
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(orderRepository.existsByUserAndOrderItemsProductAndStatus(
              user, product, OrderStatus.DELIVERED))
          .thenReturn(true);
      when(productRatingRepository.findByUserAndProduct(user, product))
          .thenReturn(Optional.empty());
      when(productRatingRepository.save(any(ProductRating.class)))
          .thenAnswer(
              invocation -> {
                ProductRating pr = invocation.getArgument(0);
                pr.setId(UUID.randomUUID());
                return pr;
              });

      when(ratingMapper.productRatingToRatingResponse(any(ProductRating.class)))
          .thenAnswer(
              invocation -> {
                ProductRating pr = invocation.getArgument(0);
                return new RatingResponse(
                    pr.getProduct().getId(),
                    pr.getProduct().getId(),
                    pr.getRatingEnum().getRating());
              });

      // Act
      RatingResponse response = ratingService.sendProductRating(user, ratingRequest);

      // Assert
      assertNotNull(response);
      assertEquals(product.getId(), response.productId());
      assertEquals(5, response.ratingStars());
      verify(productRatingRepository).save(any(ProductRating.class));
    }

    @Test
    void should_throw_exception_if_product_not_found() {
      // Arrange
      when(productRepository.findById(product.getId())).thenReturn(Optional.empty());

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class,
              () -> ratingService.sendProductRating(user, ratingRequest));

      assertEquals("Product not found", exception.getMessage());
      verify(productRatingRepository, never()).save(any());
    }

    @Test
    void should_throw_exception_if_user_has_not_purchased_product() {
      // Arrange
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(orderRepository.existsByUserAndOrderItemsProductAndStatus(
              user, product, OrderStatus.DELIVERED))
          .thenReturn(false);

      // Act & Assert
      UnauthorizedAccess exception =
          assertThrows(
              UnauthorizedAccess.class, () -> ratingService.sendProductRating(user, ratingRequest));

      assertEquals(
          "You can only rate products you have purchased and received.", exception.getMessage());
      verify(productRatingRepository, never()).save(any());
    }

    @Test
    void should_throw_exception_if_user_already_rated_product() {
      // Arrange
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(orderRepository.existsByUserAndOrderItemsProductAndStatus(
              user, product, OrderStatus.DELIVERED))
          .thenReturn(true);
      when(productRatingRepository.findByUserAndProduct(user, product))
          .thenReturn(Optional.of(new ProductRating()));

      // Act & Assert
      ResourceAlreadyExistsException exception =
          assertThrows(
              ResourceAlreadyExistsException.class,
              () -> ratingService.sendProductRating(user, ratingRequest));

      assertEquals("You have already rated this product.", exception.getMessage());
      verify(productRatingRepository, never()).save(any());
    }

    @Test
    void should_throw_exception_if_rating_value_is_invalid() {
      // Arrange
      RatingRequest invalidRequest = new RatingRequest(product.getId(), 10);
      when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
      when(orderRepository.existsByUserAndOrderItemsProductAndStatus(
              user, product, OrderStatus.DELIVERED))
          .thenReturn(true);
      when(productRatingRepository.findByUserAndProduct(user, product))
          .thenReturn(Optional.empty());

      // Act & Assert
      IllegalArgumentException exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> ratingService.sendProductRating(user, invalidRequest));

      assertEquals("Invalid rating value: 10", exception.getMessage());
      verify(productRatingRepository, never()).save(any());
    }
  }

  @Nested
  class GetVendorRating {
    @Test
    void should_return_average_rating_when_ratings_exist() {
      // Arrange
      UUID vendorId = UUID.randomUUID();
      when(productRatingRepository.getAverageRatingByVendorId(vendorId)).thenReturn(4.5);

      // Act
      Double averageRating = ratingService.getVendorRating(vendorId);

      // Assert
      assertEquals(4.5, averageRating);
    }

    @Test
    void should_return_zero_when_no_ratings_exist() {
      // Arrange
      UUID vendorId = UUID.randomUUID();
      when(productRatingRepository.getAverageRatingByVendorId(vendorId)).thenReturn(null);

      // Act
      Double averageRating = ratingService.getVendorRating(vendorId);

      // Assert
      assertEquals(0.0, averageRating);
    }
  }
}
