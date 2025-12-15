package ecom.unit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ecom.entity.Product;
import ecom.entity.StockMovement;
import ecom.model.StockReason;
import ecom.model.StockType;
import ecom.repository.StockMovementRepository;
import ecom.service.StockService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockServiceUnitTest {

  @Mock private StockMovementRepository stockMovementRepository;
  @InjectMocks private StockService stockService;

  private Product product;

  @BeforeEach
  void setUp() {
    product = Product.builder().id(UUID.randomUUID()).name("Test Product").build();
  }

  @Nested
  class createStockMovement {
    @Test
    void should_create_stock_movement_successfully() {
      // Arrange
      Integer quantity = 10;
      StockType type = StockType.IN;
      StockReason reason = StockReason.NEW;

      // Act
      stockService.createStockMovement(product, quantity, type, reason);

      // Assert
      verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }
  }
}
