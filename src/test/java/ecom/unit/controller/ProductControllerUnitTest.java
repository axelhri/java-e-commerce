package ecom.unit.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecom.controller.ProductController;
import ecom.dto.AllProductsResponse;
import ecom.dto.ProductRequest;
import ecom.dto.ProductResponse;
import ecom.interfaces.ProductServiceInterface;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductControllerUnitTest {

  @Mock private ProductServiceInterface productService;

  @InjectMocks private ProductController productController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private ProductRequest productRequest;
  private ProductResponse productResponse;
  private UUID testId;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(productController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();

    objectMapper = new ObjectMapper();
    testId = UUID.randomUUID();

    productRequest =
        new ProductRequest(
            "Test Product", 100, "Test Description", 10, UUID.randomUUID(), UUID.randomUUID());

    productResponse =
        new ProductResponse(
            testId, "Test Product", 100, "Test Description", "slug", 10, List.of(), null);
  }

  @Nested
  class CreateProduct {
    @Test
    void should_create_product_successfully() throws Exception {
      // Arrange
      when(productService.createProduct(any(ProductRequest.class), anyList()))
          .thenReturn(productResponse);

      MockMultipartFile file =
          new MockMultipartFile(
              "files", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());

      MockMultipartFile productPart =
          new MockMultipartFile(
              "product",
              "",
              MediaType.APPLICATION_JSON_VALUE,
              objectMapper.writeValueAsBytes(productRequest));

      // Act & Assert
      mockMvc
          .perform(
              multipart("/api/v1/products")
                  .file(productPart)
                  .file(file)
                  .contentType(MediaType.MULTIPART_FORM_DATA))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.status").value(201))
          .andExpect(jsonPath("$.message").value("Product created successfully"))
          .andExpect(jsonPath("$.data.product_name").value("Test Product"));

      verify(productService).createProduct(any(ProductRequest.class), anyList());
    }
  }

  @Nested
  class GetAllProducts {
    @Test
    void should_get_all_products_without_category_filter() throws Exception {
      // Arrange
      AllProductsResponse response =
          new AllProductsResponse(
              testId, "Test Product", 100, 10, "http://example.com/image.png", 4.5);
      Page<AllProductsResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

      when(productService.getAllProducts(eq(null), eq(null), any(Pageable.class))).thenReturn(page);

      // Act & Assert
      mockMvc
          .perform(get("/api/v1/products").contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value(200))
          .andExpect(jsonPath("$.message").value("Products fetched successfully"))
          .andExpect(jsonPath("$.data.content[0].product_id").value(testId.toString()))
          .andExpect(jsonPath("$.data.content[0].product_name").value("Test Product"))
          .andExpect(jsonPath("$.data.totalElements").value(1));

      verify(productService).getAllProducts(eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void should_get_products_with_category_filter() throws Exception {
      // Arrange
      UUID categoryId = UUID.randomUUID();
      AllProductsResponse response =
          new AllProductsResponse(
              testId, "Test Product", 100, 10, "http://example.com/image.png", 4.5);
      Page<AllProductsResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

      when(productService.getAllProducts(eq(categoryId), eq(null), any(Pageable.class)))
          .thenReturn(page);

      // Act & Assert
      mockMvc
          .perform(
              get("/api/v1/products")
                  .param("categoryId", categoryId.toString())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value(200))
          .andExpect(jsonPath("$.data.content[0].product_id").value(testId.toString()))
          .andExpect(jsonPath("$.data.content[0].product_name").value("Test Product"));

      verify(productService).getAllProducts(eq(categoryId), eq(null), any(Pageable.class));
    }

    @Test
    void should_get_products_with_search_filter() throws Exception {
      // Arrange
      String searchTerm = "laptop";
      AllProductsResponse response =
          new AllProductsResponse(
              testId, "Gaming Laptop", 100, 10, "http://example.com/image.png", 4.5);
      Page<AllProductsResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

      when(productService.getAllProducts(nullable(UUID.class), eq(searchTerm), any(Pageable.class)))
          .thenReturn(page);

      // Act & Assert
      mockMvc
          .perform(
              get("/api/v1/products")
                  .param("search", searchTerm)
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value(200))
          .andExpect(jsonPath("$.data.content[0].product_name").value("Gaming Laptop"));

      verify(productService)
          .getAllProducts(nullable(UUID.class), eq(searchTerm), any(Pageable.class));
    }
  }

  @Nested
  class GetProductById {
    @Test
    void should_get_product_by_id_successfully() throws Exception {
      // Arrange
      when(productService.getProductById(testId)).thenReturn(productResponse);

      // Act & Assert
      mockMvc
          .perform(get("/api/v1/products/{id}", testId).contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value(200))
          .andExpect(jsonPath("$.message").value("Product fetched successfully"))
          .andExpect(jsonPath("$.data.product_name").value("Test Product"));

      verify(productService).getProductById(testId);
    }
  }
}
