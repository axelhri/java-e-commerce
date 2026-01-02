package neora.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import neora.dto.ProductStock;
import neora.entity.Product;
import neora.entity.StockMovement;
import neora.interfaces.StockServiceInterface;
import neora.model.StockReason;
import neora.model.StockType;
import neora.repository.StockMovementRepository;
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
