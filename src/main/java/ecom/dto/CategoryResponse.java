package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record CategoryResponse(
    @JsonProperty("category_id") UUID id, @JsonProperty("category_name") String name) {}
