package neora.interfaces;

import neora.dto.ShippingAddressRequest;
import neora.entity.ShippingAddress;

public interface ShippingAddressServiceInterface {
  ShippingAddress createShippingAddress(ShippingAddressRequest request);
}
