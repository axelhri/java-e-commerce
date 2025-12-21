package ecom.unit.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.controller.VendorController;
import ecom.dto.ApiResponse;
import ecom.dto.VendorRequest;
import ecom.entity.Vendor;
import ecom.interfaces.VendorServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class VendorControllerUnitTest {

  @Mock private VendorServiceInterface vendorService;

  @InjectMocks private VendorController vendorController;

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
    void createVendorShouldReturnCode200() {
      // Arrange
      when(vendorService.createVendor(vendorRequest)).thenReturn(vendor);

      // Act
      ResponseEntity<ApiResponse<VendorRequest>> response =
          vendorController.createVendor(vendorRequest);

      // Assert
      verify(vendorService, times(1)).createVendor(vendorRequest);
      assertNotNull(response);
      assertEquals(HttpStatus.CREATED, response.getStatusCode());
      assertEquals("Vendor created successfully", response.getBody().message());
    }
  }
}
