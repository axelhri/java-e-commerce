package neora.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import neora.dto.CloudinaryResponse;
import neora.interfaces.CloudinaryServiceInterface;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class CloudinaryService implements CloudinaryServiceInterface {

  private final Cloudinary cloudinary;

  @Override
  public CloudinaryResponse upload(MultipartFile file, String folder) throws IOException {
    @SuppressWarnings("unchecked")
    Map<String, Object> result =
        (Map<String, Object>)
            cloudinary
                .uploader()
                .upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "auto", "folder", folder, "tags", folder));

    return new CloudinaryResponse(
        (String) result.get("public_id"), (String) result.get("secure_url"));
  }

  @Override
  public List<CloudinaryResponse> uploadMultiple(List<MultipartFile> files, String folder)
      throws IOException {
    List<CloudinaryResponse> responses = new ArrayList<>();
    for (MultipartFile file : files) {
      responses.add(upload(file, folder));
    }
    return responses;
  }

  @Override
  public Map<String, Object> delete(String publicId) throws IOException {
    return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
  }
}
