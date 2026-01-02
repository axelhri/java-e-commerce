package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record VendorResponse(
    @Schema(
            description = "Vendor unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("vendor_id")
        UUID id,
    @Schema(
            description = "Vendor name",
            example = "TechStore",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("vendor_name")
        String name,
    @Schema(
            description = "Vendor profile image URL",
            example = "http://res.cloudinary.com/...",
            accessMode = Schema.AccessMode.READ_ONLY,
            format = "uri")
        @JsonProperty("vendor_image")
        String imageUrl) {}
