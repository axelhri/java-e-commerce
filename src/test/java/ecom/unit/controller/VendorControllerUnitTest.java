package ecom.unit.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.controller.VendorController;
import ecom.dto.ApiResponse;
import ecom.dto.VendorRequest;
import ecom.dto.VendorResponse;
import ecom.interfaces.VendorServiceInterface;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class VendorControllerUnitTest {

  @Mock private VendorServiceInterface vendorService;

  @InjectMocks private VendorController vendorController;

  @Nested
  class createVendorUnitTest {
    private VendorRequest vendorRequest;
    private VendorResponse vendorResponse;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
      vendorRequest = new VendorRequest("HyperX");
      vendorResponse = new VendorResponse(UUID.randomUUID(), "HyperX");
      mockFile = mock(MultipartFile.class);
    }

    @Test
    void createVendorShouldReturnCode201() throws IOException {
      // Arrange
      when(vendorService.createVendor(eq(vendorRequest), eq(mockFile))).thenReturn(vendorResponse);

      // Act
      ResponseEntity<ApiResponse<VendorResponse>> response =
          vendorController.createVendor(vendorRequest, mockFile);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.CREATED, response.getStatusCode());

      ApiResponse<VendorResponse> body = response.getBody();
      assertNotNull(body);
      assertEquals("Vendor created successfully", body.message());
      assertEquals(vendorResponse.id(), body.data().id());
      assertEquals(vendorResponse.name(), body.data().name());

      verify(vendorService, times(1)).createVendor(vendorRequest, mockFile);
    }
  }
}
