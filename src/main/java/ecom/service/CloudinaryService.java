package ecom.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import ecom.dto.CloudinaryResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class CloudinaryService {

  private final Cloudinary cloudinary;

  public CloudinaryResponse upload(MultipartFile file) throws IOException {
    @SuppressWarnings("unchecked")
    Map<String, Object> result =
        (Map<String, Object>)
            cloudinary
                .uploader()
                .upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));

    return new CloudinaryResponse((String) result.get("public_id"), (String) result.get("url"));
  }

  public List<CloudinaryResponse> uploadMultiple(List<MultipartFile> files) throws IOException {
    List<CloudinaryResponse> responses = new ArrayList<>();
    for (MultipartFile file : files) {
      responses.add(upload(file));
    }
    return responses;
  }

  public Map<String, Object> delete(String publicId) throws IOException {
    @SuppressWarnings("unchecked")
    Map<String, Object> result =
        (Map<String, Object>) cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    return result;
  }
}
