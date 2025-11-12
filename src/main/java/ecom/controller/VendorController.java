package ecom.controller;

import ecom.dto.ApiResponse;
import ecom.dto.VendorRequest;
import ecom.interfaces.VendorServiceInterface;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/vendors")
public class VendorController {
  private final VendorServiceInterface vendorService;

  @PostMapping
  public ResponseEntity<ApiResponse> createVendor(@Valid @RequestBody VendorRequest dto) {
    vendorService.createVendor(dto);
    return ResponseEntity.ok(new ApiResponse(true, "Vendor created successfully."));
  }
}
