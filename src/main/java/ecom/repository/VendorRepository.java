package ecom.repository;

import ecom.entity.Vendor;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorRepository extends JpaRepository<Vendor, UUID> {
  boolean existsByName(String name);
}
