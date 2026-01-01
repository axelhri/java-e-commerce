package neora.service;

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
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class VendorService implements VendorServiceInterface {

  private final VendorRepository vendorRepository;
  private final VendorMapper vendorMapper;
  private final CloudinaryServiceInterface cloudinaryService;
  private final VendorImageRepository vendorImageRepository;

  @Override
  @Transactional
  public VendorResponse createVendor(VendorRequest vendorRequest, MultipartFile file)
      throws IOException {
    Vendor vendor = vendorMapper.toVendorEntity(vendorRequest);
    try {
      Vendor savedVendor = vendorRepository.save(vendor);
      if (file != null && !file.isEmpty()) {
        CloudinaryResponse response =
            cloudinaryService.upload(file, "vendors/vendor_" + savedVendor.getId());

        VendorImage vendorImage = new VendorImage();
        vendorImage.setImageUrl(response.url());
        vendorImage.setCloudinaryImageId(response.publicId());
        vendorImage.setVendor(savedVendor);

        vendorImageRepository.save(vendorImage);

        savedVendor.setVendorImage(vendorImage);
      }

      return new VendorResponse(
          savedVendor.getId(), savedVendor.getName(), savedVendor.getVendorImage().getImageUrl());

    } catch (DataIntegrityViolationException e) {

      throw new ResourceAlreadyExistsException("A vendor with this name already exists");
    }
  }
}
