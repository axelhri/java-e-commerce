package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import ecom.dto.ProductRequest;
import ecom.dto.ProductResponse;
import ecom.entity.Category;
import ecom.entity.Product;
import ecom.entity.Vendor;
import ecom.exception.ResourceNotFoundException;
import ecom.interfaces.CloudinaryServiceInterface;
import ecom.interfaces.StockServiceInterface;
import ecom.mapper.ProductMapper;
import ecom.repository.CategoryRepository;
import ecom.repository.ProductImageRepository;
import ecom.repository.ProductRepository;
import ecom.repository.VendorRepository;
import ecom.service.ProductService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
  @InjectMocks private ProductService productService;

  private Product product;
  private ProductRequest productRequest;
  private UUID categoryId;
  private UUID vendorId;

  @BeforeEach
  void setUp() {
    categoryId = UUID.randomUUID();
    vendorId = UUID.randomUUID();
    product = Product.builder().id(UUID.randomUUID()).name("Test Product").price(100).build();
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
      when(productRepository.findAll(pageable)).thenReturn(productPage);
      when(stockService.getCurrentStock(any(Product.class))).thenReturn(50);

      // Act
      Page<ProductResponse> result = productService.getAllProducts(null, pageable);

      // Assert
      assertEquals(1, result.getTotalElements());
      assertEquals(50, result.getContent().get(0).stock());
    }

    @Test
    void should_get_products_by_category_when_category_is_not_null() {
      // Arrange
      Pageable pageable = Pageable.unpaged();
      Page<Product> productPage = new PageImpl<>(List.of(product));
      when(productRepository.findByCategoryId(eq(categoryId), eq(pageable)))
          .thenReturn(productPage);
      when(stockService.getCurrentStock(any(Product.class))).thenReturn(50);

      // Act
      Page<ProductResponse> result = productService.getAllProducts(categoryId, pageable);

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
}
