package neora.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import neora.dto.ShippingAddressRequest;
import neora.entity.ShippingAddress;
import neora.mapper.ShippingAddressMapper;
import neora.repository.ShippingAddressRepository;
import neora.service.ShippingAddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShippingAddressServiceUnitTest {

  @Mock private ShippingAddressRepository shippingAddressRepository;
  @Mock private ShippingAddressMapper shippingAddressMapper;
  @InjectMocks private ShippingAddressService shippingAddressService;

  private ShippingAddressRequest request;
  private ShippingAddress shippingAddress;

  @BeforeEach
  void setUp() {
    request = new ShippingAddressRequest("John", "Doe", "123 Main St", "12345", "NY", "USA");
    shippingAddress =
        ShippingAddress.builder()
            .id(UUID.randomUUID())
            .firstName("John")
            .lastName("Doe")
            .addressLine("123 Main St")
            .postalCode("12345")
            .state("NY")
            .country("USA")
            .build();
  }

  @Nested
  class CreateShippingAddress {

    @Test
    void should_create_shipping_address_successfully() {
      // Arrange
      when(shippingAddressMapper.shippingAddressToEntity(request)).thenReturn(shippingAddress);
      when(shippingAddressRepository.save(shippingAddress)).thenReturn(shippingAddress);

      // Act
      ShippingAddress result = shippingAddressService.createShippingAddress(request);

      // Assert
      assertNotNull(result);
      assertEquals(shippingAddress.getId(), result.getId());
      assertEquals(shippingAddress.getFirstName(), result.getFirstName());
      assertEquals(shippingAddress.getLastName(), result.getLastName());
      assertEquals(shippingAddress.getAddressLine(), result.getAddressLine());
      assertEquals(shippingAddress.getPostalCode(), result.getPostalCode());
      assertEquals(shippingAddress.getState(), result.getState());
      assertEquals(shippingAddress.getCountry(), result.getCountry());

      verify(shippingAddressMapper).shippingAddressToEntity(request);
      verify(shippingAddressRepository).save(shippingAddress);
    }
  }
}
