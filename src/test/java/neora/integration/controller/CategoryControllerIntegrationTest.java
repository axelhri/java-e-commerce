package neora.integration.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.UUID;
import neora.config.JwtAuthenticationFilter;
import neora.config.RateLimitingFilter;
import neora.controller.CategoryController;
import neora.dto.CategoryRequest;
import neora.interfaces.CategoryServiceInterface;
import neora.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerIntegrationTest {
  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CategoryServiceInterface categoryService;
  @MockitoBean private RateLimitingFilter rateLimitingFilter;

  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockitoBean private JwtService jwtService;

  @Test
  void createCategoryShouldReturnSuccess200Ok() throws Exception {
    // Arrange
    CategoryRequest request = new CategoryRequest("Electronics", Set.of(UUID.randomUUID()));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("Category created successfully"));
  }

  @Test
  void createCategoryShouldReturnBadRequestIfValidationFails() throws Exception {
    // Arrange
    CategoryRequest request = new CategoryRequest("Electronics!!", Set.of(UUID.randomUUID()));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(
            jsonPath("$.name")
                .value("Category name must only contain letters, spaces and hyphens"));
  }
}
