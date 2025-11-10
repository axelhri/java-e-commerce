package ecom.integration.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecom.dto.AuthenticationRequest;
import ecom.dto.AuthenticationResponse;
import ecom.interfaces.AuthenticationServiceInterface;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AuthenticationServiceInterface authenticationService;

  @Nested
  class registerIntegrationTest {
    @Test
    void registerShouldReturn201Created() throws Exception {
      AuthenticationRequest request = new AuthenticationRequest("test@example.com", "Password123!");

      AuthenticationResponse response = new AuthenticationResponse("jwt-token", UUID.randomUUID());

      when(authenticationService.register(any(AuthenticationRequest.class))).thenReturn(response);

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
      AuthenticationRequest authRequest =
          new AuthenticationRequest("test.example.com", "Password123!");

      mockMvc
          .perform(
              post("/api/v1/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(authRequest)))
          .andExpect(status().isBadRequest());
    }

    @Test
    void registerShouldReturn400IfPasswordValidationFails() throws Exception {
      AuthenticationRequest authRequest = new AuthenticationRequest("test@example.com", "pass123");

      mockMvc
          .perform(
              post("/api/v1/auth/register")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(authRequest)))
          .andExpect(status().isBadRequest());
    }
  }
}
