package ecom.controller;

import ecom.dto.ApiRestResponse;
import ecom.dto.VendorRequest;
import ecom.dto.VendorResponse;
import ecom.interfaces.VendorServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/vendors")
@Tag(name = "Vendors", description = "Endpoints for managing vendors")
public class VendorController {
  private final VendorServiceInterface vendorService;

  @Operation(
      summary = "Create a new vendor",
      description = "Creates a new vendor with a profile image. Requires ADMIN role.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Vendor created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiRestResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied (Admin only)",
            content = @Content),
        @ApiResponse(
            responseCode = "409",
            description = "Vendor already exists",
            content = @Content)
      })
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiRestResponse<VendorResponse>> createVendor(
      @Parameter(description = "Vendor details in JSON format", required = true)
          @Valid
          @RequestPart("vendor")
          VendorRequest dto,
      @Parameter(description = "Vendor profile image", required = true) @RequestPart("file")
          MultipartFile file)
      throws IOException {
    VendorResponse vendor = vendorService.createVendor(dto, file);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            new ApiRestResponse<>(
                Instant.now(), HttpStatus.CREATED.value(), "Vendor created successfully", vendor));
  }
}
