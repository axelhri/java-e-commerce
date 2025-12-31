package ecom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductStock {
  @Schema(
      description = "Product unique identifier",
      example = "123e4567-e89b-12d3-a456-426614174000")
  private UUID productId;

  @Schema(description = "Stock quantity", example = "50")
  private Integer stock;
}
