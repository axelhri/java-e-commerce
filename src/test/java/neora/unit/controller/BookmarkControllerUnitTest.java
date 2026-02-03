package neora.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import neora.config.JwtAuthenticationFilter;
import neora.config.RateLimitingFilter;
import neora.controller.BookmarkController;
import neora.dto.BookmarkResponse;
import neora.dto.ManageBookmarkRequest;
import neora.entity.User;
import neora.exception.ResourceNotFoundException;
import neora.interfaces.BookmarkServiceInterface;
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

@WebMvcTest(BookmarkController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookmarkControllerUnitTest {

  @MockitoBean private BookmarkServiceInterface bookmarkService;
  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private RateLimitingFilter rateLimitingFilter;

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private User user;
  private ManageBookmarkRequest validRequest;
  private BookmarkResponse bookmarkResponse;

  @BeforeEach
  void setUp() {
    user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
    validRequest = new ManageBookmarkRequest(UUID.randomUUID());
    bookmarkResponse =
        new BookmarkResponse(UUID.randomUUID(), validRequest.productId(), user.getId());

    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @Nested
  class BookmarkProduct {

    @Test
    void should_bookmark_product_successfully_and_return_201() throws Exception {
      // Arrange
      when(bookmarkService.bookmarkProduct(any(ManageBookmarkRequest.class), any(User.class)))
          .thenReturn(bookmarkResponse);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/bookmarks")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validRequest)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.bookmark_id").value(bookmarkResponse.bookmarkId().toString()))
          .andExpect(jsonPath("$.data.product_id").value(bookmarkResponse.productId().toString()));
    }

    @Test
    void should_return_404_if_product_not_found() throws Exception {
      // Arrange
      when(bookmarkService.bookmarkProduct(any(ManageBookmarkRequest.class), any(User.class)))
          .thenThrow(new ResourceNotFoundException("Product not found"));

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/bookmarks")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validRequest)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void should_return_400_if_request_is_invalid() throws Exception {
      // Arrange
      ManageBookmarkRequest invalidRequest = new ManageBookmarkRequest(null);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/bookmarks")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest());
    }
  }
}
