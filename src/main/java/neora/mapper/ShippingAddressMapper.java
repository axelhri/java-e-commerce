package neora.mapper;

import lombok.AllArgsConstructor;
import neora.dto.*;
import neora.entity.ShippingAddress;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ShippingAddressMapper {

  public ShippingAddress shippingAddressToEntity(@NonNull ShippingAddressRequest dto) {
    return ShippingAddress.builder()
        .firstName(dto.firstName())
        .lastName(dto.lastName())
        .addressLine(dto.addressLine())
        .postalCode(dto.postalCode())
        .state(dto.state())
        .country(dto.country())
        .build();
  }

  public ShippingAddressResponse shippingAddressToResponse(
      @NonNull ShippingAddress shippingAddress) {
    return new ShippingAddressResponse(
        shippingAddress.getId(),
        shippingAddress.getFirstName(),
        shippingAddress.getLastName(),
        shippingAddress.getAddressLine(),
        shippingAddress.getPostalCode(),
        shippingAddress.getState(),
        shippingAddress.getCountry());
  }
}
