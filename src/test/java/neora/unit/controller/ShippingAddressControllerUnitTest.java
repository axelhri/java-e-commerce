package neora.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import neora.config.JwtAuthenticationFilter;
import neora.controller.ShippingAddressController;
import neora.dto.ShippingAddressRequest;
import neora.dto.ShippingAddressResponse;
import neora.interfaces.ShippingAddressServiceInterface;
import neora.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ShippingAddressController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShippingAddressControllerUnitTest {

  @MockitoBean private ShippingAddressServiceInterface shippingAddressService;
  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
  @MockitoBean private JwtService jwtService;

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  private ShippingAddressRequest validRequest;
  private ShippingAddressResponse response;

  @BeforeEach
  void setUp() {
    validRequest = new ShippingAddressRequest("John", "Doe", "123 Main St", "12345", "NY", "USA");
    response =
        new ShippingAddressResponse(
            UUID.randomUUID(), "John", "Doe", "123 Main St", "12345", "NY", "USA");
  }

  @Nested
  class CreateShippingAddress {

    @Test
    void should_create_address_successfully_and_return_201() throws Exception {
      // Arrange
      when(shippingAddressService.createShippingAddress(any(), eq(validRequest)))
          .thenReturn(response);

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/shipping-addresses")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(validRequest)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.shipping_adress_id").value(response.id().toString()))
          .andExpect(jsonPath("$.data.first_name").value("John"))
          .andExpect(jsonPath("$.data.last_name").value("Doe"));
    }

    @Test
    void should_return_400_when_request_is_invalid() throws Exception {
      // Arrange
      ShippingAddressRequest invalidRequest = new ShippingAddressRequest("", "", "", "", "", "");

      // Act & Assert
      mockMvc
          .perform(
              post("/api/v1/shipping-addresses")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(invalidRequest)))
          .andExpect(status().isBadRequest());
    }
  }
}
