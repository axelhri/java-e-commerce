package ecom.interfaces;

import ecom.dto.CloudinaryResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryServiceInterface {
  CloudinaryResponse upload(MultipartFile file, String folder) throws IOException;

  List<CloudinaryResponse> uploadMultiple(List<MultipartFile> files, String folder)
      throws IOException;

  Map<String, Object> delete(String publicId) throws IOException;
}
