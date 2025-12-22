package ecom.interfaces;

import ecom.dto.VendorRequest;
import ecom.dto.VendorResponse;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface VendorServiceInterface {
  VendorResponse createVendor(VendorRequest vendorRequest, MultipartFile file) throws IOException;
}
