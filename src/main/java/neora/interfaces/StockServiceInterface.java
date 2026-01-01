package neora.interfaces;

import neora.entity.Product;
import neora.model.StockReason;
import neora.model.StockType;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface StockServiceInterface {
  void createStockMovement(Product product, Integer quantity, StockType type, StockReason reason);

  Integer getCurrentStock(Product product);

  Map<UUID, Integer> getStocks(List<UUID> productIds);
}
