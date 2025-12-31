package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record CategoryResponse(
    @Schema(
            description = "Category unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("category_id")
        UUID id,
    @Schema(
            description = "Category name",
            example = "Electronics",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("category_name")
        String name) {}
