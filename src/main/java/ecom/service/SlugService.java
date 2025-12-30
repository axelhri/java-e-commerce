package ecom.service;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SlugService {
  private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
  private static final Pattern WHITESPACE = Pattern.compile("\\s+");
  private static final int MAX_LENGTH = 80;

  public String generateSlug(String input) {
    if (input == null) return null;

    String nowhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
    String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
    String slug = NON_LATIN.matcher(normalized).replaceAll("").toLowerCase(Locale.ENGLISH);

    slug = slug.replaceAll("-+", "-");
    slug = slug.replaceAll("(^-|-$)", "");

    if (slug.length() > MAX_LENGTH) {
      slug = slug.substring(0, MAX_LENGTH);
      slug = slug.replaceAll("-$", "");
    }

    return slug;
  }
}
