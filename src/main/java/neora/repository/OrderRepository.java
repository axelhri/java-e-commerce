package neora.repository;

import java.util.List;
import java.util.UUID;
import neora.entity.Order;
import neora.entity.Product;
import neora.entity.User;
import neora.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
  List<Order> findByUser(User user);

  List<Order> findByUserAndStatus(User user, OrderStatus status);

  boolean existsByUserAndOrderItemsProductAndStatus(User user, Product product, OrderStatus status);
}
