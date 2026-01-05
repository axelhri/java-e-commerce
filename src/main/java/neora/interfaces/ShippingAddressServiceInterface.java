package neora.interfaces;

import neora.dto.ShippingAddressRequest;
import neora.dto.ShippingAddressResponse;
import neora.entity.User;

public interface ShippingAddressServiceInterface {
  ShippingAddressResponse createShippingAddress(User user, ShippingAddressRequest request);
}
