package neora.service;

import lombok.AllArgsConstructor;
import neora.dto.ShippingAddressRequest;
import neora.entity.ShippingAddress;
import neora.interfaces.ShippingAddressServiceInterface;
import neora.mapper.ShippingAddressMapper;
import neora.repository.ShippingAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ShippingAddressService implements ShippingAddressServiceInterface {
  private final ShippingAddressRepository shippingAddressRepository;
  private final ShippingAddressMapper shippingAddressMapper;

  @Override
  @Transactional
  public ShippingAddress createShippingAddress(ShippingAddressRequest request) {
    ShippingAddress shippingAddress = shippingAddressMapper.shippingAddressToEntity(request);

    return shippingAddressRepository.save(shippingAddress);
  }
}
