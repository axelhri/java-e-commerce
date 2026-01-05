package neora.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import neora.dto.ShippingAddressRequest;
import neora.dto.ShippingAddressResponse;
import neora.entity.ShippingAddress;
import neora.entity.User;
import neora.exception.ResourceAlreadyExistsException;
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
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ShippingAddressServiceUnitTest {

  @Mock private ShippingAddressRepository shippingAddressRepository;
  @Mock private ShippingAddressMapper shippingAddressMapper;
  @InjectMocks private ShippingAddressService shippingAddressService;

  private User user;
  private ShippingAddressRequest request;
  private ShippingAddress shippingAddress;
  private ShippingAddressResponse response;

  @BeforeEach
  void setUp() {
    user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
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
            .user(user)
            .build();
    response =
        new ShippingAddressResponse(
            shippingAddress.getId(), "John", "Doe", "123 Main St", "12345", "NY", "USA");
  }

  @Nested
  class CreateShippingAddress {

    @Test
    void should_create_shipping_address_successfully() {
      // Arrange
      when(shippingAddressMapper.shippingAddressToEntity(request)).thenReturn(shippingAddress);
      when(shippingAddressRepository.save(shippingAddress)).thenReturn(shippingAddress);
      when(shippingAddressMapper.shippingAddressToResponse(shippingAddress)).thenReturn(response);

      // Act
      ShippingAddressResponse result = shippingAddressService.createShippingAddress(user, request);

      // Assert
      assertNotNull(result);
      assertEquals(response.id(), result.id());
      assertEquals(response.firstName(), result.firstName());
      verify(shippingAddressRepository).save(shippingAddress);
    }

    @Test
    void should_throw_exception_if_user_already_has_address() {
      // Arrange
      user.setShippingAddress(new ShippingAddress());

      // Act & Assert
      ResourceAlreadyExistsException exception =
          assertThrows(
              ResourceAlreadyExistsException.class,
              () -> shippingAddressService.createShippingAddress(user, request));

      assertEquals("A shipping address already exists for this user", exception.getMessage());
      verify(shippingAddressRepository, never()).save(any());
    }

    @Test
    void should_throw_exception_on_data_integrity_violation() {
      // Arrange
      when(shippingAddressMapper.shippingAddressToEntity(request)).thenReturn(shippingAddress);
      when(shippingAddressRepository.save(shippingAddress))
          .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

      // Act & Assert
      ResourceAlreadyExistsException exception =
          assertThrows(
              ResourceAlreadyExistsException.class,
              () -> shippingAddressService.createShippingAddress(user, request));

      assertEquals("A shipping address already exists for this user", exception.getMessage());
    }
  }
}
