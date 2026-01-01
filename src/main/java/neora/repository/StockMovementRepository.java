package neora.repository;

import neora.dto.ProductStock;
import neora.entity.StockMovement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
  @Query(
      """
      SELECT new neora.dto.ProductStock(
          sm.product.id,
          CAST(SUM(CASE WHEN sm.type = 'IN' THEN sm.quantity ELSE -sm.quantity END) AS int)
      )
      FROM StockMovement sm
      WHERE sm.product.id IN :productIds
      GROUP BY sm.product.id
  """)
  List<ProductStock> getStockForProducts(List<UUID> productIds);

  @Query(
      "SELECT SUM(CASE WHEN sm.type = neora.model.StockType.IN THEN sm.quantity "
          + "ELSE -sm.quantity END) "
          + "FROM StockMovement sm WHERE sm.product.id = :productId")
  Integer getStockForProduct(@Param("productId") UUID productId);
}
