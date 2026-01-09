package neora.interfaces;

import java.io.IOException;
import neora.dto.VendorRequest;
import neora.dto.VendorResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VendorServiceInterface {
  VendorResponse createVendor(VendorRequest vendorRequest, MultipartFile file) throws IOException;
}
