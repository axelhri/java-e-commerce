package ecom.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Rating {
  ONE_STAR(1),
  TWO_STAR(2),
  THREE_STAR(3),
  FOUR_STAR(4),
  FIVE_STAR(5);

  @Getter private final Integer rating;
}
