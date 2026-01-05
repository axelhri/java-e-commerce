package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record ShippingAddressResponse(
    @Schema(
            description = "Unique identifier of the shipping address",
            example = "123e4567-e89b-12d3-a456-426614174000")
        @JsonProperty("shipping_adress_id")
        UUID id,
    @Schema(description = "First name of the recipient", example = "John")
        @JsonProperty("first_name")
        String firstName,
    @Schema(description = "Last name of the recipient", example = "Doe") @JsonProperty("last_name")
        String lastName,
    @Schema(description = "Address line", example = "123 Main St") @JsonProperty("address_line")
        String addressLine,
    @Schema(description = "Postal code", example = "10001") @JsonProperty("postal_code")
        String postalCode,
    @Schema(description = "State or region", example = "NY") @JsonProperty("state") String state,
    @Schema(description = "Country name", example = "USA") @JsonProperty("country")
        String country) {}
