package ecom.interfaces;

import ecom.dto.ProductRequest;
import ecom.entity.Product;

public interface ProductServiceInterface {
  Product createProduct(ProductRequest productRequest);
}
