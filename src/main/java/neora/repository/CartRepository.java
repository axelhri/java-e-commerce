package neora.repository;

import java.util.UUID;
import neora.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, UUID> {}
