package neora.interfaces;

import neora.dto.VendorRequest;
import neora.dto.VendorResponse;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface VendorServiceInterface {
  VendorResponse createVendor(VendorRequest vendorRequest, MultipartFile file) throws IOException;
}
