package ecom.mapper;

import ecom.dto.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {
  public <T> PagedResponse<T> toPagedResponse(@NonNull Page<T> page) {
    return new PagedResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isLast());
  }
}
