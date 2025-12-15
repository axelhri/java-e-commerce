package ecom.repository;

import ecom.entity.CartItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
  Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId);

  List<CartItem> findByCartId(UUID cartId);

  void deleteByCartId(UUID cartId);
}
