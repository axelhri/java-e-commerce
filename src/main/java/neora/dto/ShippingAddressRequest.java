package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShippingAddressRequest(
    @JsonProperty("first_name")
        @NotBlank(message = "First name is required.")
        @Size(min = 2, max = 100)
        String firstName,
    @JsonProperty("last_name")
        @NotBlank(message = "Last name is required.")
        @Size(min = 2, max = 100)
        String lastName,
    @JsonProperty("address_line")
        @NotBlank(message = "Address line is required.")
        @Size(min = 10, max = 255)
        String addressLine,
    @JsonProperty("postal_code")
        @NotBlank(message = "Postal code is required.")
        @Size(min = 3, max = 20)
        String postalCode,
    @JsonProperty("state") @NotBlank(message = "State is required.") @Size(min = 2, max = 255)
        String state,
    @JsonProperty("country") @NotBlank(message = "Country is required.") @Size(min = 2, max = 255)
        String country) {}
