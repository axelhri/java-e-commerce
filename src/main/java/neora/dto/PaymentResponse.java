package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentResponse(
    @JsonProperty("order") OrderResponse order,
    @Schema(
            description = "Stripe client secret for payment confirmation",
            example = "pi_123456789_secret_987654321")
        @JsonProperty("client_secret")
        String clientSecret) {}
