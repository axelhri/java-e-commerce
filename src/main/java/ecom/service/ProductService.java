package ecom.service;

import ecom.dto.ProductRequest;
import ecom.entity.Category;
import ecom.entity.Product;
import ecom.entity.Vendor;
import ecom.exception.ResourceNotFoundException;
import ecom.mapper.ProductMapper;
import ecom.repository.CategoryRepository;
import ecom.repository.ProductRepository;
import ecom.repository.VendorRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final VendorRepository vendorRepository;
  private final ProductMapper productMapper;

  public Product createProduct(ProductRequest productRequest) {
    Product product = productMapper.productToEntity(productRequest);
    Category category =
        categoryRepository
            .findById(productRequest.category())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    Vendor vendor =
        vendorRepository
            .findById(productRequest.vendor())
            .orElseThrow(() -> new ResourceNotFoundException("Vendor not found."));

    product.setCategory(category);
    product.setVendor(vendor);

    return productRepository.save(product);
  }
}
