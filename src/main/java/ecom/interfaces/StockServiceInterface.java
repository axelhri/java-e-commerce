package ecom.interfaces;

import ecom.entity.Product;
import ecom.model.StockReason;
import ecom.model.StockType;

public interface StockServiceInterface {
  void createStockMovement(Product product, Integer quantity, StockType type, StockReason reason);

  Integer getCurrentStock(Product product);
}
