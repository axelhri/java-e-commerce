package neora.interfaces;

import neora.dto.AllProductsResponse;
import neora.dto.ProductRequest;
import neora.dto.ProductResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProductServiceInterface {
  ProductResponse createProduct(ProductRequest productRequest, List<MultipartFile> images)
      throws IOException;

  Page<AllProductsResponse> getAllProducts(UUID categoryId, String search, Pageable pageable);

  ProductResponse getProductById(UUID productId);

  ProductResponse getProductBySlug(String slug);
}
