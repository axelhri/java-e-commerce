package neora.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
@Slf4j
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
    log.info("Attempting to create a new product with name: {}", productRequest.name());

    Product product = productMapper.productToEntity(productRequest);
    Category category =
        categoryRepository
            .findById(productRequest.category())
            .orElseThrow(
                () -> {
                  log.error("Category not found for ID: {}", productRequest.category());
                  return new ResourceNotFoundException("Category not found.");
                });
    Vendor vendor =
        vendorRepository
            .findById(productRequest.vendor())
            .orElseThrow(
                () -> {
                  log.error("Vendor not found for ID: {}", productRequest.vendor());
                  return new ResourceNotFoundException("Vendor not found.");
                });

    product.setCategory(category);
    product.setVendor(vendor);

    String slug = slugService.generateSlug(product.getName());
    product.setSlug(slug);
    log.debug("Generated slug '{}' for product name '{}'", slug, product.getName());

    Product savedProduct = productRepository.save(product);
    log.info(
        "Product {} saved successfully with ID: {}", savedProduct.getName(), savedProduct.getId());

    if (images != null && !images.isEmpty()) {
      log.debug("Uploading {} images for product ID: {}", images.size(), savedProduct.getId());
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
      log.debug(
          "Saved {} product images for product ID: {}", productImages.size(), savedProduct.getId());
      savedProduct.setImages(productImages);
    }

    stockService.createStockMovement(
        savedProduct, productRequest.stock(), StockType.IN, StockReason.NEW);
    log.info(
        "Initial stock of {} recorded for product ID: {}",
        productRequest.stock(),
        savedProduct.getId());

    return productMapper.toResponse(savedProduct, productRequest.stock());
  }

  @Override
  public Page<AllProductsResponse> getAllProducts(
      UUID categoryId, String search, Pageable pageable) {
    log.info(
        "Fetching products with categoryId: {}, search: '{}', page: {}",
        categoryId,
        search,
        pageable.getPageNumber());
    Specification<Product> spec =
        Specification.allOf(
            ProductSpecification.hasCategory(categoryId),
            ProductSpecification.nameContains(search));

    Page<Product> products = productRepository.findAll(spec, pageable);
    log.info(
        "Found {} products on page {}", products.getNumberOfElements(), pageable.getPageNumber());

    return getAllProductsResponses(products);
  }

  @Override
  public ProductResponse getProductById(UUID productId) {
    log.info("Fetching product with ID: {}", productId);
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(
                () -> {
                  log.error("Product not found for ID: {}", productId);
                  return new ResourceNotFoundException("Product not found.");
                });
    log.info("Found product: {}", product.getName());
    return productMapper.toResponse(product, stockService.getCurrentStock(product));
  }

  @Override
  public ProductResponse getProductBySlug(String slug) {
    log.info("Fetching product with slug: {}", slug);
    Product product =
        productRepository
            .findBySlug(slug)
            .orElseThrow(
                () -> {
                  log.error("Product not found for slug: {}", slug);
                  return new ResourceNotFoundException("Product not found.");
                });
    log.info("Found product: {}", product.getName());
    return productMapper.toResponse(product, stockService.getCurrentStock(product));
  }

  @Override
  public Page<AllProductsResponse> getProductsByCategory(UUID categoryId, Pageable pageable) {
    log.info(
        "Fetching products for category ID: {}, page: {}", categoryId, pageable.getPageNumber());
    Category category =
        categoryRepository
            .findById(categoryId)
            .orElseThrow(
                () -> {
                  log.error("Category not found for ID: {}", categoryId);

                  return new ResourceNotFoundException("Category not found");
                });
    Page<Product> products = productRepository.findAllProductsByCategory(category, pageable);
    log.info(
        "Found {} products in category '{}' on page {}",
        products.getNumberOfElements(),
        category.getName(),
        pageable.getPageNumber());
    return getAllProductsResponses(products);
  }

  private Page<AllProductsResponse> getAllProductsResponses(Page<Product> products) {
    if (products.isEmpty()) {
      return Page.empty();
    }
    List<UUID> productIds = products.map(Product::getId).toList();
    log.debug("Fetching stock and ratings for {} product IDs", productIds.size());

    Map<UUID, Integer> stockMap = stockService.getStocks(productIds);
    Map<UUID, Double> ratingMap = ratingService.getRatings(productIds);
    log.debug("Successfully fetched stock and ratings");

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
}
