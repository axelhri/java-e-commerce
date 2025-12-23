package ecom.interfaces;

import ecom.dto.AllProductsResponse;
import ecom.dto.ProductRequest;
import ecom.dto.ProductResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ProductServiceInterface {
  ProductResponse createProduct(ProductRequest productRequest, List<MultipartFile> images)
      throws IOException;

  Page<AllProductsResponse> getAllProducts(UUID categoryId, Pageable pageable);

  ProductResponse getProductById(UUID productId);
}
