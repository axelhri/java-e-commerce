package neora.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import neora.dto.CategoryRequest;
import neora.dto.CategoryResponse;
import neora.entity.Category;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
  public Category categoryToEntity(@NonNull CategoryRequest dto) {
    return Category.builder().name(dto.name()).build();
  }

  public CategoryResponse mapToCategoryResponse(Category category) {
    if (category == null) {
      return null;
    }
    List<CategoryResponse> children =
        Optional.ofNullable(category.getChildrenCategory()).orElse(Collections.emptySet()).stream()
            .map(this::mapToCategoryResponse)
            .toList();

    return new CategoryResponse(category.getId(), category.getName(), List.copyOf(children));
  }
}
