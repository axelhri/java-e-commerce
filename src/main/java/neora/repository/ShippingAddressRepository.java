package neora.repository;

import java.util.UUID;
import neora.entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, UUID> {}
