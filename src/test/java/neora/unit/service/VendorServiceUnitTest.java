package neora.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.UUID;
import neora.dto.CloudinaryResponse;
import neora.dto.VendorRequest;
import neora.dto.VendorResponse;
import neora.entity.Vendor;
import neora.exception.ResourceAlreadyExistsException;
import neora.interfaces.CloudinaryServiceInterface;
import neora.mapper.VendorMapper;
import neora.repository.VendorImageRepository;
import neora.repository.VendorRepository;
import neora.service.VendorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class VendorServiceUnitTest {

  @Mock private VendorRepository vendorRepository;
  @Mock private VendorMapper vendorMapper;
  @Mock private CloudinaryServiceInterface cloudinaryService;
  @Mock private VendorImageRepository vendorImageRepository;
  @InjectMocks private VendorService vendorService;

  @Nested
  class createVendorUnitTest {
    private VendorRequest vendorRequest;
    private Vendor vendor;

    @BeforeEach
    void setUp() {
      vendorRequest = new VendorRequest("HyperX");

      neora.entity.VendorImage dummyImage = new neora.entity.VendorImage();
      dummyImage.setImageUrl(null);

      vendor =
          Vendor.builder()
              .id(UUID.randomUUID())
              .name(vendorRequest.name())
              .vendorImage(dummyImage)
              .build();
    }

    @Test
    void createVendorWithoutImageShouldReturnVendorResponse() throws IOException {
      // Arrange
      when(vendorMapper.toVendorEntity(vendorRequest)).thenReturn(vendor);
      when(vendorRepository.save(vendor)).thenReturn(vendor);

      // Act
      VendorResponse response = vendorService.createVendor(vendorRequest, null);

      // Assert
      assertNotNull(response);
      assertEquals("HyperX", response.name());
      assertNull(response.imageUrl());
      verify(vendorRepository, times(1)).save(vendor);
    }

    @Test
    void createVendorWithImageShouldUploadAndReturnResponse() throws IOException {
      // Arrange
      MultipartFile mockFile = mock(MultipartFile.class);
      CloudinaryResponse cloudResponse = new CloudinaryResponse("url_image", "public_id");

      when(mockFile.isEmpty()).thenReturn(false);
      when(vendorMapper.toVendorEntity(vendorRequest)).thenReturn(vendor);
      when(vendorRepository.save(vendor)).thenReturn(vendor);
      when(cloudinaryService.upload(any(MultipartFile.class), anyString()))
          .thenReturn(cloudResponse);

      // Act
      VendorResponse response = vendorService.createVendor(vendorRequest, mockFile);

      // Assert
      assertNotNull(response);
      verify(cloudinaryService).upload(eq(mockFile), contains("vendors/vendor_" + vendor.getId()));
      verify(vendorImageRepository).save(any());
      assertEquals("HyperX", response.name());
    }

    @Test
    void createVendorShouldThrowExceptionWhenVendorAlreadyExists() {
      // Arrange
      when(vendorMapper.toVendorEntity(vendorRequest)).thenReturn(vendor);
      when(vendorRepository.save(vendor))
          .thenThrow(new DataIntegrityViolationException("Duplicate vendor"));

      // Act & Assert
      assertThrows(
          ResourceAlreadyExistsException.class,
          () -> vendorService.createVendor(vendorRequest, null));
    }
  }
}
