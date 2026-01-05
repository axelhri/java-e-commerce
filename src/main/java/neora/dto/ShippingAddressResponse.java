package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record ShippingAddressResponse(
    @JsonProperty("shipping_adress_id") UUID id,
    @JsonProperty("first_name") String firstName,
    @JsonProperty("last_name") String lastName,
    @JsonProperty("address_line") String addressLine,
    @JsonProperty("postal_code") String postalCode,
    @JsonProperty("state") String state,
    @JsonProperty("country") String country) {}
