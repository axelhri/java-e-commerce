package neora.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record ApiRestResponse<T>(
    @Schema(
            description = "Timestamp of the response",
            example = "2023-10-27T10:00:00Z",
            accessMode = Schema.AccessMode.READ_ONLY)
        Instant timestamp,
    @Schema(
            description = "HTTP status code",
            example = "200",
            accessMode = Schema.AccessMode.READ_ONLY)
        int status,
    @Schema(
            description = "Response message",
            example = "Operation successful",
            accessMode = Schema.AccessMode.READ_ONLY)
        String message,
    @Schema(description = "Response data payload", accessMode = Schema.AccessMode.READ_ONLY)
        T data) {}
