package ecom.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SubCategoryRequest(
    @JsonProperty("category_name")
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name must not exceed 100 characters")
        String name,
    @JsonProperty("parent_id") @NotNull(message = "La catégorie doit avoir une catégorie parent")
        UUID parentCategory) {}
