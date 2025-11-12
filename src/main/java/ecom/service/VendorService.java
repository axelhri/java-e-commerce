package ecom.service;

import ecom.dto.VendorRequest;
import ecom.entity.Vendor;
import ecom.exception.ResourceAlreadyExists;
import ecom.interfaces.VendorServiceInterface;
import ecom.mapper.VendorMapper;
import ecom.repository.VendorRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class VendorService implements VendorServiceInterface {

  private final VendorRepository vendorRepository;
  private final VendorMapper vendorMapper;

  @Override
  public Vendor createVendor(VendorRequest vendorRequest) {
    Vendor vendor = vendorMapper.toVendorEntity(vendorRequest);
    try {
      return vendorRepository.save(vendor);

    } catch (DataIntegrityViolationException e) {

      throw new ResourceAlreadyExists("A vendor with this name already exists");
    }
  }
}
