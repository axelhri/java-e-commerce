package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.dto.ProductRequest;
import ecom.dto.ProductResponse;
import ecom.entity.Category;
import ecom.entity.Product;
import ecom.entity.Vendor;
import ecom.exception.ResourceNotFoundException;
import ecom.interfaces.StockServiceInterface;
import ecom.mapper.ProductMapper;
import ecom.repository.CategoryRepository;
import ecom.repository.ProductRepository;
import ecom.repository.VendorRepository;
import ecom.service.ProductService;
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

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

  @Mock private ProductRepository productRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private VendorRepository vendorRepository;
  @Mock private ProductMapper productMapper;
  @Mock private StockServiceInterface stockService;
  @InjectMocks private ProductService productService;

  private Product product;
  private ProductRequest productRequest;
  private Category category;
  private Vendor vendor;
  private UUID categoryId;

  @BeforeEach
  void setUp() {
    categoryId = UUID.randomUUID();
    productRequest =
        new ProductRequest(
            "Black trench coat",
            80000,
            "Black trench coat very comfortable made out of wool",
            100,
            UUID.randomUUID(),
            UUID.randomUUID());
    product =
        Product.builder()
            .name(productRequest.name())
            .price(productRequest.price())
            .description(productRequest.description())
            .build();
    category = Category.builder().name("coat").build();
    vendor = Vendor.builder().name("Jules").build();
  }

  @Nested
  class createProduct {

    @Test
    void createProductShouldCreateSuccessfully() {
      // Arrange
      when(productMapper.productToEntity(productRequest)).thenReturn(product);
      when(categoryRepository.findById(productRequest.category()))
          .thenReturn(Optional.of(category));
      when(vendorRepository.findById(productRequest.vendor())).thenReturn(Optional.of(vendor));
      when(productRepository.save(product)).thenReturn(product);

      // Act
      Product result = productService.createProduct(productRequest);

      // Assert
      assertEquals(product, result);
      assertEquals(category, result.getCategory());
      assertEquals(vendor, result.getVendor());

      verify(productRepository, times(1)).save(product);
    }

    @Test
    void createProductShouldThrowExceptionIfCategoryDoesNotExist() {
      // Arrange
      when(productMapper.productToEntity(productRequest)).thenReturn(product);
      when(categoryRepository.findById(productRequest.category())).thenReturn(Optional.empty());

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class, () -> productService.createProduct(productRequest));

      assertEquals("Category not found.", exception.getMessage());
      verify(productRepository, never()).save(product);
    }

    @Test
    void createProductShouldThrowExceptionIfVendorDoesNotExist() {
      // Arrange
      when(productMapper.productToEntity(productRequest)).thenReturn(product);
      when(categoryRepository.findById(productRequest.category()))
          .thenReturn(Optional.of(category));
      when(vendorRepository.findById(productRequest.vendor())).thenReturn(Optional.empty());

      // Act & Assert
      ResourceNotFoundException exception =
          assertThrows(
              ResourceNotFoundException.class, () -> productService.createProduct(productRequest));

      assertEquals("Vendor not found.", exception.getMessage());
      verify(productRepository, never()).save(product);
    }

    @Test
    void createProductShouldThrowWhenMapperReturnsNull() {
      // Arrange
      when(productMapper.productToEntity(productRequest)).thenReturn(null);
      when(categoryRepository.findById(productRequest.category()))
          .thenReturn(Optional.of(category));
      when(vendorRepository.findById(productRequest.vendor())).thenReturn(Optional.of(vendor));

      // Act & Assert
      assertThrows(NullPointerException.class, () -> productService.createProduct(productRequest));

      verify(productMapper).productToEntity(productRequest);

      verify(productRepository, never()).save(any());
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
      when(stockService.getCurrentStock(product)).thenReturn(50);

      // Act
      Page<ProductResponse> result = productService.getAllProducts(null, pageable);

      // Assert
      assertEquals(1, result.getTotalElements());
      assertEquals(50, result.getContent().get(0).stock());
      verify(productRepository, times(1)).findAll(pageable);
      verify(productRepository, never()).findByCategoryId(any(), any());
    }

    @Test
    void should_get_products_by_category_when_category_is_not_null() {
      // Arrange
      Pageable pageable = Pageable.unpaged();
      Page<Product> productPage = new PageImpl<>(List.of(product));
      when(productRepository.findByCategoryId(categoryId, pageable)).thenReturn(productPage);
      when(stockService.getCurrentStock(product)).thenReturn(50);

      // Act
      Page<ProductResponse> result = productService.getAllProducts(categoryId, pageable);

      // Assert
      assertEquals(1, result.getTotalElements());
      assertEquals(50, result.getContent().get(0).stock());
      verify(productRepository, never()).findAll(pageable);
      verify(productRepository, times(1)).findByCategoryId(categoryId, pageable);
    }
  }

  @Nested
  class GetProductById {
    @Test
    void should_return_product_when_found() {
      // Arrange
      UUID productId = UUID.randomUUID();
      Product foundProduct =
          Product.builder().id(productId).name("Found Product").price(100).build();
      when(productRepository.findById(productId)).thenReturn(Optional.of(foundProduct));
      when(stockService.getCurrentStock(foundProduct)).thenReturn(20);

      // Act
      ProductResponse result = productService.getProductById(productId);

      // Assert
      assertNotNull(result);
      assertEquals(productId, result.id());
      assertEquals("Found Product", result.name());
      assertEquals(20, result.stock());
    }
  }
}
