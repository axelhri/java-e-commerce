package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import ecom.dto.ProductRequest;
import ecom.entity.Category;
import ecom.entity.Product;
import ecom.entity.Vendor;
import ecom.mapper.ProductMapper;
import ecom.repository.CategoryRepository;
import ecom.repository.ProductRepository;
import ecom.repository.VendorRepository;
import ecom.service.ProductService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductServiceUnitTest {

  @Mock private ProductRepository productRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private VendorRepository vendorRepository;
  @Mock private ProductMapper productMapper;
  @InjectMocks private ProductService productService;

  private Product product;
  private ProductRequest productRequest;
  private Category category;
  private Vendor vendor;

  @BeforeEach
  void setUp() {
    productRequest =
        new ProductRequest(
            "Black trench coat",
            80000,
            "Black trench coat very comfortable made out of wool",
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
  }
}
