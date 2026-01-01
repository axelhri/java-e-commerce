package neora.unit.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import neora.config.JwtAuthenticationFilter;
import neora.controller.EmailController;
import neora.interfaces.EmailServiceInterface;
import neora.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmailController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmailControllerUnitTest {

  @MockitoBean private EmailServiceInterface emailService;
  @MockitoBean private JwtService jwtService;

  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void should_confirm_email_successfully() throws Exception {
    // Arrange
    String token = "valid-token";
    doNothing().when(emailService).confirmEmail(token);

    // Act & Assert
    mockMvc
        .perform(get("/api/v1/email/confirm").param("token", token))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("Email confirmed successfully"))
        .andExpect(jsonPath("$.data").value("Email confirmed successfully"));

    verify(emailService).confirmEmail(token);
  }
}
