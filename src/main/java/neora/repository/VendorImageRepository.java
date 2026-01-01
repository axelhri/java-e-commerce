package neora.repository;

import neora.entity.VendorImage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorImageRepository extends JpaRepository<VendorImage, UUID> {}
