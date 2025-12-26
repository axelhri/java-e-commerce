package ecom.service;

import ecom.dto.ProductStock;
import ecom.entity.Product;
import ecom.entity.StockMovement;
import ecom.interfaces.StockServiceInterface;
import ecom.model.StockReason;
import ecom.model.StockType;
import ecom.repository.StockMovementRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
    Integer totalStock = stockMovementRepository.getStockForProduct(product.getId());
    return (totalStock != null) ? totalStock : 0;
  }

  @Override
  public Map<UUID, Integer> getStocks(List<UUID> productIds) {
    return stockMovementRepository.getStockForProducts(productIds).stream()
        .collect(Collectors.toMap(ProductStock::getProductId, ProductStock::getStock));
  }
}
