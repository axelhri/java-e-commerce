package neora.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record RefreshTokenResponse(
    @Schema(
            description = "New JWT Access Token",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonIgnore
        String accessToken,
    @Schema(
            description = "Refresh Token used to obtain new access token",
            example = "d9b2d63d-a233-4123-85f8-77654321e321",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonIgnore
        String refreshToken,
    @Schema(
            description = "User unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty("user_id")
        UUID userId) {}
