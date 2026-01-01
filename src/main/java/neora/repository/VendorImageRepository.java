package neora.repository;

import java.util.UUID;
import neora.entity.VendorImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorImageRepository extends JpaRepository<VendorImage, UUID> {}
