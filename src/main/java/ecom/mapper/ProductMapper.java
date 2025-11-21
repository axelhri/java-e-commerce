package ecom.mapper;

import ecom.dto.ProductRequest;
import ecom.entity.Product;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
  public Product productToEntity(@NonNull ProductRequest dto) {
    return Product.builder()
        .name(dto.name())
        .price(dto.price())
        .description(dto.description())
        .build();
  }
}
