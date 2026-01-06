package neora.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;
import neora.dto.AllProductsResponse;
import neora.dto.ProductRequest;
import neora.dto.ProductResponse;
import neora.entity.Category;
import neora.entity.Product;
import neora.entity.ProductImage;
import neora.entity.Vendor;
import neora.exception.ResourceNotFoundException;
import neora.interfaces.CloudinaryServiceInterface;
import neora.interfaces.RatingServiceInterface;
import neora.interfaces.StockServiceInterface;
import neora.mapper.ProductMapper;
import neora.repository.CategoryRepository;
import neora.repository.ProductImageRepository;
import neora.repository.ProductRepository;
import neora.repository.VendorRepository;
import neora.service.ProductService;
import neora.service.SlugService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

  @Mock private ProductRepository productRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private VendorRepository vendorRepository;
  @Mock private ProductMapper productMapper;
  @Mock private StockServiceInterface stockService;
  @Mock private CloudinaryServiceInterface cloudinaryService;
  @Mock private ProductImageRepository productImageRepository;
  @Mock private RatingServiceInterface ratingService;
  @Mock private SlugService slugService;

  @InjectMocks private ProductService productService;

  private Product product;
  private ProductRequest productRequest;
  private UUID categoryId;
  private UUID vendorId;

  @BeforeEach
  void setUp() {
    categoryId = UUID.randomUUID();
    vendorId = UUID.randomUUID();

    ProductImage dummyImage = new ProductImage();
    dummyImage.setImageUrl("http://example.com/image.png");

    product =
        Product.builder()
            .id(UUID.randomUUID())
            .name("Test Product")
            .price(100)
            .images(List.of(dummyImage))
            .build();

    productRequest = new ProductRequest("Test", 100, "Desc", 10, categoryId, vendorId);
  }

  @Nested
  class CreateProduct {
    @Test
    void should_create_product_successfully() throws IOException {
      // Arrange
      List<MultipartFile> emptyFiles = Collections.emptyList();

      when(productMapper.productToEntity(any(ProductRequest.class))).thenReturn(product);
      when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Category()));
      when(vendorRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Vendor()));
      when(productRepository.save(any(Product.class))).thenReturn(product);
      when(slugService.generateSlug(anyString())).thenReturn("test-slug");

      ProductResponse expectedResponse =
          new ProductResponse(product.getId(), "Test", 100, "Desc", 10, List.of(), null);
      when(productMapper.toResponse(any(Product.class), anyInt())).thenReturn(expectedResponse);

      // Act
      ProductResponse result = productService.createProduct(productRequest, emptyFiles);

      // Assert
      assertNotNull(result);
      assertEquals(product.getId(), result.id());
      verify(stockService).createStockMovement(any(), anyInt(), any(), any());
    }

    @Test
    void should_throw_exception_if_category_not_found() {
      // Arrange
      List<MultipartFile> emptyFiles = Collections.emptyList();
      when(productMapper.productToEntity(any(ProductRequest.class))).thenReturn(product);
      when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(
          ResourceNotFoundException.class,
          () -> productService.createProduct(productRequest, emptyFiles));
    }

    @Test
    void should_throw_exception_if_vendor_not_found() {
      // Arrange
      List<MultipartFile> emptyFiles = Collections.emptyList();
      when(productMapper.productToEntity(any(ProductRequest.class))).thenReturn(product);
      when(categoryRepository.findById(any(UUID.class))).thenReturn(Optional.of(new Category()));
      when(vendorRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(
          ResourceNotFoundException.class,
          () -> productService.createProduct(productRequest, emptyFiles));
    }
  }

  @Nested
  class GetAllProducts {
    @Test
    void should_get_all_products_when_category_is_null() {
      // Arrange
      Pageable pageable = Pageable.unpaged();
      Page<Product> productPage = new PageImpl<>(List.of(product));

      when(productRepository.findAll(any(Specification.class), eq(pageable)))
          .thenReturn(productPage);
      when(stockService.getStocks(anyList())).thenReturn(Map.of(product.getId(), 50));
      when(ratingService.getRatings(anyList())).thenReturn(Map.of(product.getId(), 4.5));

      // Act
      Page<AllProductsResponse> result = productService.getAllProducts(null, null, pageable);

      // Assert
      assertEquals(1, result.getTotalElements());
      assertEquals(50, result.getContent().get(0).stock());
      assertEquals(4.5, result.getContent().get(0).rating());
    }

    @Test
    void should_get_products_by_category_when_category_is_not_null() {
      // Arrange
      Pageable pageable = Pageable.unpaged();
      Page<Product> productPage = new PageImpl<>(List.of(product));

      when(productRepository.findAll(any(Specification.class), eq(pageable)))
          .thenReturn(productPage);
      when(stockService.getStocks(anyList())).thenReturn(Map.of(product.getId(), 50));
      when(ratingService.getRatings(anyList())).thenReturn(Map.of(product.getId(), 4.5));

      // Act
      Page<AllProductsResponse> result = productService.getAllProducts(categoryId, null, pageable);

      // Assert
      assertEquals(1, result.getTotalElements());
      assertEquals(50, result.getContent().get(0).stock());
    }
  }

  @Nested
  class GetProductById {
    @Test
    void should_return_product_when_found() {
      // Arrange
      UUID productId = UUID.randomUUID();
      when(productRepository.findById(productId)).thenReturn(Optional.of(product));
      when(stockService.getCurrentStock(product)).thenReturn(20);

      ProductResponse mockResponse =
          new ProductResponse(product.getId(), "Test", 100, "Desc", 20, List.of(), null);
      when(productMapper.toResponse(product, 20)).thenReturn(mockResponse);

      // Act
      ProductResponse result = productService.getProductById(productId);

      // Assert
      assertNotNull(result);
      assertEquals(product.getId(), result.id());
    }

    @Test
    void should_throw_exception_when_product_not_found() {
      // Arrange
      UUID productId = UUID.randomUUID();
      when(productRepository.findById(productId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(productId));
    }
  }

  @Nested
  class GetProductBySlug {
    @Test
    void should_return_product_when_found_by_slug() {
      // Arrange
      String slug = "test-product";
      when(productRepository.findBySlug(slug)).thenReturn(Optional.of(product));
      when(stockService.getCurrentStock(product)).thenReturn(20);

      ProductResponse expectedResponse =
          new ProductResponse(product.getId(), "Test Product", 100, "Desc", 20, List.of(), null);
      when(productMapper.toResponse(product, 20)).thenReturn(expectedResponse);

      // Act
      ProductResponse result = productService.getProductBySlug(slug);

      // Assert
      assertNotNull(result);
      assertEquals(product.getId(), result.id());
      assertEquals(20, result.stock());
    }

    @Test
    void should_throw_exception_when_product_not_found() {
      // Arrange
      String slug = "test-product";
      when(productRepository.findBySlug(slug)).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(ResourceNotFoundException.class, () -> productService.getProductBySlug(slug));
    }
  }

  @Nested
  class GetProductsByCategory {
    @Test
    void should_return_products_by_category() {
      // Arrange
      Pageable pageable = Pageable.unpaged();
      Page<Product> productPage = new PageImpl<>(List.of(product));
      Category category = new Category();
      category.setId(categoryId);

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
      when(productRepository.findAllProductsByCategory(category, pageable)).thenReturn(productPage);
      when(stockService.getStocks(anyList())).thenReturn(Map.of(product.getId(), 10));
      when(ratingService.getRatings(anyList())).thenReturn(Map.of(product.getId(), 4.5));

      // Act
      Page<AllProductsResponse> result = productService.getProductsByCategory(categoryId, pageable);

      // Assert
      assertNotNull(result);
      assertEquals(1, result.getContent().size());
      assertEquals(product.getId(), result.getContent().get(0).id());
    }

    @Test
    void should_throw_exception_when_category_not_found() {
      // Arrange
      Pageable pageable = Pageable.unpaged();
      when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

      // Act & Assert
      assertThrows(
          ResourceNotFoundException.class,
          () -> productService.getProductsByCategory(categoryId, pageable));
    }
  }
}
