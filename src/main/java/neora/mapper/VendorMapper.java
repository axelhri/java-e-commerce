package neora.mapper;

import neora.dto.VendorRequest;
import neora.entity.Vendor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class VendorMapper {

  public Vendor toVendorEntity(@NonNull VendorRequest dto) {
    return Vendor.builder().name(dto.name()).build();
  }
}
