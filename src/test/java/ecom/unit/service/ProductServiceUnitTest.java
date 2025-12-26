package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import ecom.dto.*;
import ecom.entity.*;
import ecom.interfaces.CloudinaryServiceInterface;
import ecom.interfaces.RatingServiceInterface;
import ecom.interfaces.StockServiceInterface;
import ecom.mapper.ProductMapper;
import ecom.model.StockReason;
import ecom.model.StockType;
import ecom.repository.CategoryRepository;
import ecom.repository.ProductImageRepository;
import ecom.repository.ProductRepository;
import ecom.repository.VendorRepository;
import ecom.service.ProductService;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
import org.springframework.data.domain.PageRequest;
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

  @InjectMocks private ProductService productService;

  private Product product;
  private ProductRequest productRequest;
  private UUID productId;
  private UUID categoryId;
  private UUID vendorId;

  @BeforeEach
  void setUp() {
    productId = UUID.randomUUID();
    categoryId = UUID.randomUUID();
    vendorId = UUID.randomUUID();

    ProductImage dummyImage = new ProductImage();
    dummyImage.setImageUrl("http://example.com/image.png");

    product =
        Product.builder()
            .id(productId)
            .name("Test Product")
            .price(100)
            .images(new java.util.ArrayList<>(List.of(dummyImage))) // Mutable list
            .build();

    productRequest = new ProductRequest("Test", 100, "Desc", 10, categoryId, vendorId);
  }

  @Nested
  class CreateProduct {
    @Test
    void should_create_product_successfully_without_images() throws IOException {
      // Arrange
      when(productMapper.productToEntity(productRequest)).thenReturn(product);
      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(new Category()));
      when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(new Vendor()));
      when(productRepository.save(any(Product.class))).thenReturn(product);

      ProductResponse expectedResponse =
          new ProductResponse(productId, "Test", 100, "Desc", 10, List.of(), null);
      when(productMapper.toResponse(eq(product), eq(10))).thenReturn(expectedResponse);

      // Act
      ProductResponse result = productService.createProduct(productRequest, null);

      // Assert
      assertNotNull(result);
      verify(stockService).createStockMovement(product, 10, StockType.IN, StockReason.NEW);
      verify(cloudinaryService, never()).uploadMultiple(anyList(), anyString());
    }

    @Test
    void should_upload_images_when_provided() throws IOException {
      // Arrange
      MultipartFile file = mock(MultipartFile.class);
      List<MultipartFile> images = List.of(file);
      CloudinaryResponse cloudRes = new CloudinaryResponse("url", "publicId");

      when(productMapper.productToEntity(productRequest)).thenReturn(product);
      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(new Category()));
      when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(new Vendor()));
      when(productRepository.save(any(Product.class))).thenReturn(product);
      when(cloudinaryService.uploadMultiple(eq(images), anyString())).thenReturn(List.of(cloudRes));

      // Act
      productService.createProduct(productRequest, images);

      // Assert
      verify(productImageRepository).saveAll(anyList());
      verify(cloudinaryService).uploadMultiple(eq(images), contains(productId.toString()));
    }
  }

  @Nested
  class GetAllProducts {
    @Test
    void should_get_all_products_with_batch_stock_and_ratings() {
      // Arrange
      Pageable pageable = PageRequest.of(0, 10);
      Page<Product> productPage = new PageImpl<>(List.of(product));

      when(productRepository.findAll(any(Specification.class), eq(pageable)))
          .thenReturn(productPage);

      when(stockService.getStocks(anyList())).thenReturn(Map.of(productId, 50));
      when(ratingService.getRatings(anyList())).thenReturn(Map.of(productId, 4.5));

      // Act
      Page<AllProductsResponse> result =
          productService.getAllProducts(categoryId, "searchQuery", pageable);

      // Assert
      assertEquals(1, result.getTotalElements());
      AllProductsResponse dto = result.getContent().get(0);
      assertEquals(50, dto.stock());
      assertEquals(4.5, dto.rating());
      verify(stockService).getStocks(anyList());
      verify(ratingService).getRatings(anyList());
    }
  }

  @Nested
  class GetProductById {
    @Test
    void should_return_product_with_current_stock() {
      // Arrange
      when(productRepository.findById(productId)).thenReturn(Optional.of(product));
      when(stockService.getCurrentStock(product)).thenReturn(25);

      ProductResponse mockResponse =
          new ProductResponse(productId, "Test", 100, "Desc", 25, List.of(), null);
      when(productMapper.toResponse(product, 25)).thenReturn(mockResponse);

      // Act
      ProductResponse result = productService.getProductById(productId);

      // Assert
      assertEquals(25, result.stock());
      verify(stockService).getCurrentStock(product);
    }
  }
}
