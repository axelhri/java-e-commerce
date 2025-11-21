package ecom.mapper;

import ecom.dto.CategoryRequest;
import ecom.entity.Category;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
  public Category categoryToEntity(@NonNull CategoryRequest dto) {
    return Category.builder().name(dto.name()).build();
  }
}
