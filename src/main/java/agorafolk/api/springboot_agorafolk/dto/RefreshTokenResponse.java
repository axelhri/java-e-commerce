package agorafolk.api.springboot_agorafolk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record RefreshTokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("user_id") UUID id) {}
