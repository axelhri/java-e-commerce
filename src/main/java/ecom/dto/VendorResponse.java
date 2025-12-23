package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record VendorResponse(
    @JsonProperty("vendor_id") UUID id,
    @JsonProperty("vendor_name") String name,
    @JsonProperty("vendor_image") String imageUrl) {}
