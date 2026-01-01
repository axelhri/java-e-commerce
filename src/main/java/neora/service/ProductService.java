package neora.service;

import neora.dto.*;
import neora.entity.Category;
import neora.entity.Product;
import neora.entity.ProductImage;
import neora.entity.Vendor;
import neora.exception.ResourceNotFoundException;
import neora.interfaces.CloudinaryServiceInterface;
import neora.interfaces.ProductServiceInterface;
import neora.interfaces.RatingServiceInterface;
import neora.interfaces.StockServiceInterface;
import neora.mapper.ProductMapper;
import neora.model.StockReason;
import neora.model.StockType;
import neora.repository.CategoryRepository;
import neora.repository.ProductImageRepository;
import neora.repository.ProductRepository;
import neora.repository.VendorRepository;
import neora.specification.ProductSpecification;
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
  private final SlugService slugService;

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

    product.setSlug(slugService.generateSlug(product.getName()));

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
                product.getSlug(),
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

  @Override
  public ProductResponse getProductBySlug(String slug) {
    Product product =
        productRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

    return productMapper.toResponse(product, stockService.getCurrentStock(product));
  }
}
