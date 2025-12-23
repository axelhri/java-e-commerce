package ecom.service;

import ecom.dto.AllProductsResponse;
import ecom.dto.CloudinaryResponse;
import ecom.dto.ProductRequest;
import ecom.dto.ProductResponse;
import ecom.entity.Category;
import ecom.entity.Product;
import ecom.entity.ProductImage;
import ecom.entity.Vendor;
import ecom.exception.ResourceNotFoundException;
import ecom.interfaces.CloudinaryServiceInterface;
import ecom.interfaces.ProductServiceInterface;
import ecom.interfaces.StockServiceInterface;
import ecom.mapper.ProductMapper;
import ecom.model.StockReason;
import ecom.model.StockType;
import ecom.repository.CategoryRepository;
import ecom.repository.ProductImageRepository;
import ecom.repository.ProductRepository;
import ecom.repository.VendorRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class ProductService implements ProductServiceInterface {
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final VendorRepository vendorRepository;
  private final ProductMapper productMapper;
  private final StockServiceInterface stockService;
  private final CloudinaryServiceInterface cloudinaryService;
  private final ProductImageRepository productImageRepository;

  @Override
  @Transactional
  public ProductResponse createProduct(ProductRequest productRequest, List<MultipartFile> images)
      throws IOException {
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

    Product savedProduct = productRepository.save(product);

    if (images != null && !images.isEmpty()) {
      List<CloudinaryResponse> uploads =
          cloudinaryService.uploadMultiple(images, "products/product_" + savedProduct.getId());

      List<ProductImage> productImages = new ArrayList<>();
      for (int i = 0; i < uploads.size(); i++) {
        CloudinaryResponse res = uploads.get(i);
        ProductImage img = new ProductImage();
        img.setImageUrl(res.url());
        img.setCloudinaryImageId(res.publicId());
        img.setProduct(savedProduct);
        img.setDisplayOrder(i);
        productImages.add(img);
      }

      productImageRepository.saveAll(productImages);

      savedProduct.setImages(productImages);
    }

    stockService.createStockMovement(
        savedProduct, productRequest.stock(), StockType.IN, StockReason.NEW);

    return new ProductResponse(
        savedProduct.getId(),
        savedProduct.getName(),
        savedProduct.getPrice(),
        savedProduct.getDescription(),
        stockService.getCurrentStock(savedProduct),
        savedProduct.getImages().stream().map(ProductImage::getImageUrl).toList());
  }

  @Override
  public Page<AllProductsResponse> getAllProducts(UUID categoryId, Pageable pageable) {
    Page<Product> products;
    if (categoryId != null) {
      products = productRepository.findByCategoryId(categoryId, pageable);
    } else {
      products = productRepository.findAll(pageable);
    }

    return products.map(
        product ->
            new AllProductsResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                stockService.getCurrentStock(product),
                product.getPrimaryImage().getImageUrl()));
  }

  @Override
  public ProductResponse getProductById(UUID productId) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getPrice(),
        product.getDescription(),
        stockService.getCurrentStock(product),
        product.getImages().stream().map(ProductImage::getImageUrl).toList());
  }
}
