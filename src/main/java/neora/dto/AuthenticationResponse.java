package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record AuthenticationResponse(
    @Schema(
            description = "JWT Access Token",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("access_token")
        String accessToken,
    @Schema(
            description = "User unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("user_id")
        UUID id) {}
