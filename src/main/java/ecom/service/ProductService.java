package ecom.service;

import ecom.dto.*;
import ecom.entity.Category;
import ecom.entity.Product;
import ecom.entity.ProductImage;
import ecom.entity.Vendor;
import ecom.exception.ResourceNotFoundException;
import ecom.interfaces.CloudinaryServiceInterface;
import ecom.interfaces.ProductServiceInterface;
import ecom.interfaces.RatingServiceInterface;
import ecom.interfaces.StockServiceInterface;
import ecom.mapper.ProductMapper;
import ecom.model.StockReason;
import ecom.model.StockType;
import ecom.repository.CategoryRepository;
import ecom.repository.ProductImageRepository;
import ecom.repository.ProductRepository;
import ecom.repository.VendorRepository;
import ecom.specification.ProductSpecification;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
  private final RatingServiceInterface ratingService;

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

    return productMapper.toResponse(savedProduct, productRequest.stock());
  }

  @Override
  public Page<AllProductsResponse> getAllProducts(
      UUID categoryId, String search, Pageable pageable) {
    Specification<Product> spec =
        Specification.allOf(
            ProductSpecification.hasCategory(categoryId),
            ProductSpecification.nameContains(search));

    Page<Product> products = productRepository.findAll(spec, pageable);

    List<UUID> productIds = products.map(Product::getId).toList();

    Map<UUID, Integer> stockMap = stockService.getStocks(productIds);
    Map<UUID, Double> ratingMap = ratingService.getRatings(productIds);

    return products.map(
        product ->
            new AllProductsResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                stockMap.getOrDefault(product.getId(), 0),
                product.getPrimaryImage().getImageUrl(),
                ratingMap.getOrDefault(product.getId(), 5.0)));
  }

  @Override
  public ProductResponse getProductById(UUID productId) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

    return productMapper.toResponse(product, stockService.getCurrentStock(product));
  }
}
