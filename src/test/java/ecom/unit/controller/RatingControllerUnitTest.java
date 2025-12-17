package ecom.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecom.config.JwtAuthenticationFilter;
import ecom.controller.RatingController;
import ecom.dto.RatingRequest;
import ecom.dto.RatingResponse;
import ecom.entity.User;
import ecom.interfaces.RatingServiceInterface;
import ecom.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
class RatingControllerUnitTest {

  @MockitoBean private RatingServiceInterface ratingService;
  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockitoBean private JwtService jwtService;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private RatingRequest validRequest;
  private RatingResponse ratingResponse;

  @BeforeEach
  void setUp() {
    validRequest = new RatingRequest(UUID.randomUUID(), 5);
    ratingResponse = new RatingResponse(UUID.randomUUID(), validRequest.productId(), 5);
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
}
