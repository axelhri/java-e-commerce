package neora.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import neora.dto.ProductStock;
import neora.entity.Product;
import neora.entity.StockMovement;
import neora.model.StockReason;
import neora.model.StockType;
import neora.repository.StockMovementRepository;
import neora.service.StockService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockServiceUnitTest {

  @Mock private StockMovementRepository stockMovementRepository;

  @InjectMocks private StockService stockService;

  private Product product;
  private UUID productId;

  @BeforeEach
  void setUp() {
    productId = UUID.randomUUID();
    product = Product.builder().id(productId).name("Test Product").build();
  }

  @Nested
  class CreateStockMovement {
    @Test
    void should_create_stock_movement_successfully() {
      // Arrange
      Integer quantity = 10;
      StockType type = StockType.IN;
      StockReason reason = StockReason.NEW;

      // On utilise un ArgumentCaptor pour vérifier l'objet construit
      ArgumentCaptor<StockMovement> movementCaptor = ArgumentCaptor.forClass(StockMovement.class);

      // Act
      stockService.createStockMovement(product, quantity, type, reason);

      // Assert
      verify(stockMovementRepository).save(movementCaptor.capture());
      StockMovement savedMovement = movementCaptor.getValue();

      assertEquals(product, savedMovement.getProduct());
      assertEquals(quantity, savedMovement.getQuantity());
      assertEquals(type, savedMovement.getType());
      assertEquals(reason, savedMovement.getReason());
    }
  }

  @Nested
  class GetCurrentStock {
    @Test
    void should_return_zero_when_repository_returns_null() {
      // Arrange
      when(stockMovementRepository.getStockForProduct(productId)).thenReturn(null);

      // Act
      Integer currentStock = stockService.getCurrentStock(product);

      // Assert
      assertEquals(0, currentStock);
      verify(stockMovementRepository).getStockForProduct(productId);
    }

    @Test
    void should_return_quantity_from_repository() {
      // Arrange
      Integer expectedStock = 42;
      when(stockMovementRepository.getStockForProduct(productId)).thenReturn(expectedStock);

      // Act
      Integer currentStock = stockService.getCurrentStock(product);

      // Assert
      assertEquals(expectedStock, currentStock);
    }
  }

  @Nested
  class GetStocks {
    @Test
    void should_return_map_of_stocks() {
      // Arrange
      UUID id2 = UUID.randomUUID();
      List<UUID> ids = List.of(productId, id2);

      List<ProductStock> mockResults =
          List.of(new ProductStock(productId, 10), new ProductStock(id2, 20));

      when(stockMovementRepository.getStockForProducts(ids)).thenReturn(mockResults);

      // Act
      Map<UUID, Integer> stocks = stockService.getStocks(ids);

      // Assert
      assertEquals(2, stocks.size());
      assertEquals(10, stocks.get(productId));
      assertEquals(20, stocks.get(id2));
    }
  }
}
