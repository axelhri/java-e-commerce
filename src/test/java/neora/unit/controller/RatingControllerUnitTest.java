package neora.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import neora.config.JwtAuthenticationFilter;
import neora.config.RateLimitingFilter;
import neora.controller.RatingController;
import neora.dto.RatingRequest;
import neora.dto.RatingResponse;
import neora.entity.User;
import neora.interfaces.RatingServiceInterface;
import neora.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
class RatingControllerUnitTest {

  @MockitoBean private RatingServiceInterface ratingService;
  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
  @MockitoBean private RateLimitingFilter rateLimitingFilter;

  @MockitoBean private JwtService jwtService;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private RatingRequest validRequest;
  private RatingResponse ratingResponse;
  private User user;

  @BeforeEach
  void setUp() {
    user = User.builder().id(UUID.randomUUID()).build();
    validRequest = new RatingRequest(UUID.randomUUID(), 5);
    ratingResponse = new RatingResponse(UUID.randomUUID(), validRequest.productId(), 5);

    // Simulate authenticated user
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @Nested
  class sendProductRating {
    @Test
    void should_send_rating_and_return_201_created() throws Exception {
      // Arrange
      when(ratingService.sendProductRating(any(User.class), any(RatingRequest.class)))
          .thenReturn(ratingResponse);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/ratings/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validRequest)))
          .andExpect(status().isCreated());
    }

    @Test
    void should_return_400_bad_request_for_invalid_rating() throws Exception {
      // Arrange
      RatingRequest invalidRequest = new RatingRequest(UUID.randomUUID(), 10); // Note > 5

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/ratings/products")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  class getVendorAverageRating {

    @Test
    void should_get_vendor_rating_successfully() throws Exception {
      UUID vendorId = UUID.randomUUID();
      Double averageRating = 4.5;
      when(ratingService.getVendorRating(vendorId)).thenReturn(averageRating);

      mockMvc
          .perform(get("/api/v1/ratings/vendor/{id}", vendorId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.vendor_id").value(vendorId.toString()))
          .andExpect(jsonPath("$.data.average_rating").value(averageRating));
    }
  }
}
