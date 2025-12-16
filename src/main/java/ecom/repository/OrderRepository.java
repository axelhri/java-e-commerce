package ecom.repository;

import ecom.entity.Order;
import ecom.entity.User;
import ecom.model.OrderStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
  List<Order> findByUser(User user);

  List<Order> findByUserAndStatus(User user, OrderStatus status);
}
