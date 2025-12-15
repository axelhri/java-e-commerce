package ecom.repository;

import ecom.entity.Product;
import ecom.entity.StockMovement;
import ecom.model.StockType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

  @Query(
      "SELECT SUM(sm.quantity) FROM StockMovement sm WHERE sm.product = :product AND sm.type = :type")
  Optional<Integer> sumQuantityByProductAndType(
      @Param("product") Product product, @Param("type") StockType type);
}
