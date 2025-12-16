package ecom.interfaces;

import ecom.dto.ProductRequest;
import ecom.dto.ProductResponse;
import ecom.entity.Product;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductServiceInterface {
  Product createProduct(ProductRequest productRequest);

  Page<ProductResponse> getAllProducts(UUID categoryId, Pageable pageable);

  ProductResponse getProductById(UUID productId);
}
