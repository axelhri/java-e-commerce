package neora.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
    @Schema(
            description = "Error message details",
            example = "Resource not found",
            accessMode = Schema.AccessMode.READ_ONLY)
        String message,
    @Schema(
            description = "HTTP status code",
            example = "404",
            accessMode = Schema.AccessMode.READ_ONLY)
        int status,
    @Schema(
            description = "Timestamp of the error (epoch millis)",
            example = "1698400800000",
            accessMode = Schema.AccessMode.READ_ONLY)
        long timestamp) {}
