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

      // Act
      RatingResponse response = ratingService.sendProductRating(user, ratingRequest);

      // Assert
      assertNotNull(response);
      assertEquals(product.getId(), response.productId());
      assertEquals(5, response.ratingStars());
      verify(productRatingRepository).save(any(ProductRating.class));
    }
  }
}
