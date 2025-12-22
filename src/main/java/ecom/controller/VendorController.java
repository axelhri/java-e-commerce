package ecom.controller;

import ecom.dto.ApiResponse;
import ecom.dto.VendorRequest;
import ecom.dto.VendorResponse;
import ecom.interfaces.VendorServiceInterface;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/vendors")
public class VendorController {
  private final VendorServiceInterface vendorService;

  @PostMapping
  public ResponseEntity<ApiResponse<VendorResponse>> createVendor(
      @Valid @RequestPart("vendor") VendorRequest dto, @RequestPart("file") MultipartFile file)
      throws IOException {
    VendorResponse vendor = vendorService.createVendor(dto, file);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiResponse<>(
                Instant.now(), HttpStatus.CREATED.value(), "Vendor created successfully", vendor));
  }
}
