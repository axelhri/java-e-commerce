package ecom.interfaces;

import ecom.dto.VendorRequest;
import ecom.entity.Vendor;

public interface VendorServiceInterface {
  Vendor createVendor(VendorRequest vendorRequest);
}
