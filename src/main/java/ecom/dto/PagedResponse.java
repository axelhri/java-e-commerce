package ecom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record PagedResponse<T>(
    @Schema(description = "List of content items", accessMode = Schema.AccessMode.READ_ONLY)
        List<T> content,
    @Schema(
            description = "Current page number (0-based)",
            example = "0",
            accessMode = Schema.AccessMode.READ_ONLY)
        int page,
    @Schema(
            description = "Number of items per page",
            example = "10",
            accessMode = Schema.AccessMode.READ_ONLY)
        int size,
    @Schema(
            description = "Total number of items available",
            example = "50",
            accessMode = Schema.AccessMode.READ_ONLY)
        long totalElements,
    @Schema(
            description = "Total number of pages",
            example = "5",
            accessMode = Schema.AccessMode.READ_ONLY)
        int totalPages,
    @Schema(
            description = "Is this the last page?",
            example = "false",
            accessMode = Schema.AccessMode.READ_ONLY)
        boolean last) {}
