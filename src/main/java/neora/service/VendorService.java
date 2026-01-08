package neora.service;

import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import neora.dto.CloudinaryResponse;
import neora.dto.VendorRequest;
import neora.dto.VendorResponse;
import neora.entity.Vendor;
import neora.entity.VendorImage;
import neora.exception.ResourceAlreadyExistsException;
import neora.interfaces.CloudinaryServiceInterface;
import neora.interfaces.VendorServiceInterface;
import neora.mapper.VendorMapper;
import neora.repository.VendorImageRepository;
import neora.repository.VendorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
@Slf4j
public class VendorService implements VendorServiceInterface {

  private final VendorRepository vendorRepository;
  private final VendorMapper vendorMapper;
  private final CloudinaryServiceInterface cloudinaryService;
  private final VendorImageRepository vendorImageRepository;

  @Override
  @Transactional
  public VendorResponse createVendor(VendorRequest vendorRequest, MultipartFile file)
      throws IOException {
    log.info("Attempting to create vendor with name: {}", vendorRequest.name());
    Vendor vendor = vendorMapper.toVendorEntity(vendorRequest);
    try {
      Vendor savedVendor = vendorRepository.save(vendor);
      log.info("Vendor saved successfully with ID: {}", savedVendor.getId());

      if (file != null && !file.isEmpty()) {
        log.debug("Uploading profile image for vendor ID: {}", savedVendor.getId());
        CloudinaryResponse response =
            cloudinaryService.upload(file, "vendors/vendor_" + savedVendor.getId());

        VendorImage vendorImage = new VendorImage();
        vendorImage.setImageUrl(response.url());
        vendorImage.setCloudinaryImageId(response.publicId());
        vendorImage.setVendor(savedVendor);

        vendorImageRepository.save(vendorImage);
        log.debug("Vendor image saved successfully");

        savedVendor.setVendorImage(vendorImage);
      } else {
        log.debug("No profile image provided for vendor ID: {}", savedVendor.getId());
      }

      String imageUrl =
          savedVendor.getVendorImage() != null ? savedVendor.getVendorImage().getImageUrl() : null;
      return new VendorResponse(savedVendor.getId(), savedVendor.getName(), imageUrl);

    } catch (DataIntegrityViolationException e) {
      log.warn(
          "Vendor creation failed: A vendor with name '{}' already exists", vendorRequest.name());
      throw new ResourceAlreadyExistsException("A vendor with this name already exists");
    }
  }
}
