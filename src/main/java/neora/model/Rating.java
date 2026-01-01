package neora.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
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

  private static final Map<Integer, Rating> RATING_MAP =
      Arrays.stream(values()).collect(Collectors.toMap(Rating::getRating, r -> r));

  public static Rating fromValue(int value) {
    Rating rating = RATING_MAP.get(value);
    if (rating == null) {
      throw new IllegalArgumentException("Invalid rating value: " + value);
    }
    return rating;
  }
}
