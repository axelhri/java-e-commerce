package ecom.integration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ecom.config.PostgresTestContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import ecom.dto.AuthenticationRequest;
import ecom.dto.AuthenticationResponse;
import ecom.dto.RefreshTokenResponse;
import ecom.interfaces.AuthenticationServiceInterface;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerIntegrationTest extends PostgresTestContainer {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AuthenticationServiceInterface authenticationService;

  @Nested
  class registerIntegrationTest {
    @Test
    void registerShouldReturn201Created() throws Exception {
      // Arrange
      AuthenticationRequest request = new AuthenticationRequest("test@example.com", "Password123!");

      AuthenticationResponse response = new AuthenticationResponse("jwt-token", UUID.randomUUID());

      when(authenticationService.register(any(AuthenticationRequest.class))).thenReturn(response);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated()) // Vérifier le status 201
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.access_token").isNotEmpty())
          .andExpect(jsonPath("$.user_id").isNotEmpty());
    }

    @Test
    void registerShouldReturn400IfEmailValidationFails() throws Exception {
      // Arrange
      AuthenticationRequest authRequest =
          new AuthenticationRequest("test.example.com", "Password123!");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(authRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void registerShouldReturn400IfPasswordValidationFails() throws Exception {
      // Arrange
      AuthenticationRequest authRequest = new AuthenticationRequest("test@example.com", "pass123");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(authRequest)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  class loginIntegrationTest {
    @Test
    void loginShouldReturn200Ok() throws Exception {
      // Arrange
      AuthenticationRequest request = new AuthenticationRequest("test@example.com", "Password123!");
      AuthenticationResponse response = new AuthenticationResponse("jwt-token", UUID.randomUUID());

      when(authenticationService.login(any())).thenReturn(response);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.access_token").isNotEmpty())
          .andExpect(jsonPath("$.user_id").isNotEmpty());
    }

    @Test
    void loginShouldReturn400IfEmailValidationFails() throws Exception {
      // Arrange
      AuthenticationRequest authRequest =
          new AuthenticationRequest("test.example.com", "Password123!");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(authRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void loginShouldReturn400IfPasswordValidationFails() throws Exception {
      // Arrange
      AuthenticationRequest authRequest = new AuthenticationRequest("test@example.com", "pass123");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/login")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(authRequest)))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  class refreshTokenIntegrationTest {
    @Test
    void refreshTokenShouldReturn200Ok() throws Exception {
      // Arrange
      RefreshTokenResponse response =
          new RefreshTokenResponse("jwt-access-token", "jwt-refresh-token", UUID.randomUUID());

      when(authenticationService.refreshToken("valid-refresh-token")).thenReturn(response);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/auth/refresh-token")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer valid-refresh-token"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.access_token").isNotEmpty());
    }

    @Test
    void refreshTokenShouldReturn401IfHeaderIsMissing() throws Exception {
      // Act & Assert
      mockMvc.perform(post("/api/v1/auth/refresh-token")).andExpect(status().isUnauthorized());
    }
  }
}
