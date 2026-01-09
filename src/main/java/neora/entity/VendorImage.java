package neora.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vendors_images")
public class VendorImage extends AbstractImage {

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vendor_id", nullable = false)
  private Vendor vendor;
}
