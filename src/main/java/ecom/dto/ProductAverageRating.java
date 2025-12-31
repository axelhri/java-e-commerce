package ecom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductAverageRating {
  @Schema(
      description = "Product unique identifier",
      example = "123e4567-e89b-12d3-a456-426614174000",
      accessMode = Schema.AccessMode.READ_ONLY)
  private UUID productId;

  @Schema(
      description = "Average rating value",
      example = "4.7",
      accessMode = Schema.AccessMode.READ_ONLY)
  private Double averageRating;
}
