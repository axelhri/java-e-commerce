package neora.service;

import lombok.AllArgsConstructor;
import neora.dto.ShippingAddressRequest;
import neora.dto.ShippingAddressResponse;
import neora.entity.ShippingAddress;
import neora.entity.User;
import neora.exception.ResourceAlreadyExistsException;
import neora.interfaces.ShippingAddressServiceInterface;
import neora.mapper.ShippingAddressMapper;
import neora.repository.ShippingAddressRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ShippingAddressService implements ShippingAddressServiceInterface {
  private final ShippingAddressRepository shippingAddressRepository;
  private final ShippingAddressMapper shippingAddressMapper;

  @Override
  @Transactional
  public ShippingAddressResponse createShippingAddress(User user, ShippingAddressRequest request) {

    if (user.getShippingAddress() != null) {
      throw new ResourceAlreadyExistsException("A shipping address already exists for this user");
    }

    ShippingAddress shippingAddress = shippingAddressMapper.shippingAddressToEntity(request);
    shippingAddress.setUser(user);

    try {
      ShippingAddress savedAddress = shippingAddressRepository.save(shippingAddress);

      return shippingAddressMapper.shippingAddressToResponse(savedAddress);
    } catch (DataIntegrityViolationException e) {
      throw new ResourceAlreadyExistsException("A shipping address already exists for this user");
    }
  }
}
