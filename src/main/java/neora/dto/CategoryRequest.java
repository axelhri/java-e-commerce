package neora.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record CategoryRequest(
    @Schema(
            description = "Category name",
            example = "Electronics",
            maxLength = 100,
            pattern = "^[\\p{L} -]+$")
        @JsonProperty("category_name")
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        @Pattern(
            regexp = "^[\\p{L} -]+$",
            message = "Category name must only contain letters, spaces and hyphens")
        String name,
    @Schema(
            description = "Set of parent category IDs (for sub-categories)",
            example = "[\"123e4567-e89b-12d3-a456-426614174000\"]")
        @JsonProperty("category_parents")
        Set<UUID> parentIds) {}
