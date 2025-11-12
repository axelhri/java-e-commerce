package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VendorRequest(
    @JsonProperty("vendor_name")
        @NotBlank(message = "Vendor name is required")
        @Size(max = 100, message = "Vendor name must not exceed 100 characters")
        @Pattern(
            regexp = "^[\\p{L}\\p{N}\\s-]+$",
            message = "Vendor name can only contain letters, numbers, spaces, and hyphens")
        String name) {}
