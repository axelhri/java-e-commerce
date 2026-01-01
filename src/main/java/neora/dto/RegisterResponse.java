package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record RegisterResponse(
    @Schema(
            description = "Registration status message",
            example = "User registered successfully. Please confirm your email",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("message")
        String message,
    @Schema(
            description = "User unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("user_id")
        UUID id) {}
