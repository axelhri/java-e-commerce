package ecom.mapper;

import ecom.dto.ProductImageResponse;
import ecom.dto.ProductRequest;
import ecom.dto.ProductResponse;
import ecom.dto.VendorSummary;
import ecom.entity.Product;
import ecom.entity.ProductImage;
import ecom.entity.Vendor;
import ecom.interfaces.RatingServiceInterface;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProductMapper {
  private final RatingServiceInterface ratingService;

  public Product productToEntity(@NonNull ProductRequest dto) {
    return Product.builder()
        .name(dto.name())
        .price(dto.price())
        .description(dto.description())
        .build();
  }

  public ProductResponse toResponse(Product product, Integer currentStock) {
    Vendor vendor = product.getVendor();

    VendorSummary vendorSummary =
        new VendorSummary(
            vendor.getId(),
            vendor.getName(),
            ratingService.getVendorRating(vendor.getId()),
            vendor.getVendorImage() != null ? vendor.getVendorImage().getImageUrl() : null);

    return new ProductResponse(
        product.getId(),
        product.getName(),
        product.getPrice(),
        product.getDescription(),
        currentStock,
        mapImageResponses(product.getImages()),
        vendorSummary);
  }

  public List<ProductImageResponse> mapImageResponses(List<ProductImage> images) {
    if (images == null || images.isEmpty()) {
      return Collections.emptyList();
    }
    return images.stream()
        .map(img -> new ProductImageResponse(img.getImageUrl(), img.getDisplayOrder()))
        .toList();
  }
}
