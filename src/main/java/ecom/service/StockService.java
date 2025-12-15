package ecom.service;

import ecom.entity.Product;
import ecom.entity.StockMovement;
import ecom.interfaces.StockServiceInterface;
import ecom.model.StockReason;
import ecom.model.StockType;
import ecom.repository.StockMovementRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StockService implements StockServiceInterface {

  private final StockMovementRepository stockMovementRepository;

  @Override
  public void createStockMovement(
      Product product, Integer quantity, StockType type, StockReason reason) {
    StockMovement stockMovement =
        StockMovement.builder()
            .product(product)
            .quantity(quantity)
            .type(type)
            .reason(reason)
            .build();
    stockMovementRepository.save(stockMovement);
  }

  @Override
  public Integer getCurrentStock(Product product) {
    Integer stockIn =
        stockMovementRepository.sumQuantityByProductAndType(product, StockType.IN).orElse(0);
    Integer stockOut =
        stockMovementRepository.sumQuantityByProductAndType(product, StockType.OUT).orElse(0);
    return stockIn - stockOut;
  }
}
