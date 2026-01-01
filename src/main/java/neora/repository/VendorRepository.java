package neora.repository;

import java.util.UUID;
import neora.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {
  boolean existsByName(String name);
}
