package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShippingAddressRequest(
    @Schema(description = "First name of the recipient", example = "John")
        @JsonProperty("first_name")
        @NotBlank(message = "First name is required.")
        @Size(min = 2, max = 100)
        String firstName,
    @Schema(description = "Last name of the recipient", example = "Doe")
        @JsonProperty("last_name")
        @NotBlank(message = "Last name is required.")
        @Size(min = 2, max = 100)
        String lastName,
    @Schema(description = "Address line (street, number, etc.)", example = "123 Main St")
        @JsonProperty("address_line")
        @NotBlank(message = "Address line is required.")
        @Size(min = 10, max = 255)
        String addressLine,
    @Schema(description = "Postal code", example = "10001")
        @JsonProperty("postal_code")
        @NotBlank(message = "Postal code is required.")
        @Size(min = 3, max = 20)
        String postalCode,
    @Schema(description = "State or region", example = "NY")
        @JsonProperty("state")
        @NotBlank(message = "State is required.")
        @Size(min = 2, max = 255)
        String state,
    @Schema(description = "Country name", example = "USA")
        @JsonProperty("country")
        @NotBlank(message = "Country is required.")
        @Size(min = 2, max = 255)
        String country) {}
