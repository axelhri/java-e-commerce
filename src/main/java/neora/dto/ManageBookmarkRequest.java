package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ManageBookmarkRequest(
    @Schema(
            description = "Product unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000")
        @JsonProperty("product_id")
        @NotNull(message = "Product's id is required")
        UUID productId) {}
