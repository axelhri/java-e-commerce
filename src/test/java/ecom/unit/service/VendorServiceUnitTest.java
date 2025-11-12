package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import ecom.dto.VendorRequest;
import ecom.entity.Vendor;
import ecom.exception.ResourceAlreadyExists;
import ecom.mapper.VendorMapper;
import ecom.repository.VendorRepository;
import ecom.service.VendorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
public class VendorServiceUnitTest {

  @Mock private VendorRepository vendorRepository;
  @Mock private VendorMapper vendorMapper;
  @InjectMocks private VendorService vendorService;

  @Nested
  class createVendorUnitTest {
    private VendorRequest vendorRequest;
    private Vendor vendor;

    @BeforeEach
    void setUp() {
      vendorRequest = new VendorRequest("HyperX");
      vendor = Vendor.builder().name(vendorRequest.name()).build();
    }

    @Test
    void createVendorShouldReturnVendorSuccessfully() {
      when(vendorMapper.toVendorEntity(vendorRequest)).thenReturn(vendor);
      when(vendorRepository.save(vendor)).thenReturn(vendor);

      Vendor createdVendor = vendorService.createVendor(vendorRequest);

      assertNotNull(createdVendor);
      assertEquals(createdVendor.getName(), vendorRequest.name());
    }

    @Test
    void createVendorShouldThrowExceptionVendorAlreadyExists() {
      when(vendorMapper.toVendorEntity(vendorRequest)).thenReturn(vendor);
      when(vendorRepository.save(vendor))
          .thenThrow(new DataIntegrityViolationException("Duplicate vendor"));

      ResourceAlreadyExists exception =
          assertThrows(
              ResourceAlreadyExists.class, () -> vendorService.createVendor(vendorRequest));

      assertEquals("A vendor with this name already exists", exception.getMessage());
    }
  }
}
