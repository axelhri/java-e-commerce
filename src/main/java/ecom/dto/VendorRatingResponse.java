package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record VendorRatingResponse(
    @JsonProperty("vendor_id") UUID vendorId,
    @JsonProperty("average_rating") Double averageRating) {}
