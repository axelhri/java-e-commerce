package ecom.unit.service;

import static org.junit.jupiter.api.Assertions.*;

import ecom.service.SlugService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SlugServiceUnitTest {

  private SlugService slugService;

  @BeforeEach
  void setUp() {
    slugService = new SlugService();
  }

  @Nested
  class GenerateSlug {

    @Test
    void should_return_null_when_input_is_null() {
      // Act
      String result = slugService.generateSlug(null);

      // Assert
      assertNull(result);
    }

    @Test
    void should_return_empty_string_when_input_is_empty() {
      // Act
      String result = slugService.generateSlug("");

      // Assert
      assertEquals("", result);
    }

    @Test
    void should_trim_and_replace_whitespaces_with_dash() {
      // Act
      String result = slugService.generateSlug("  tennis   racket  ");

      // Assert
      assertEquals("tennis-racket", result);
    }

    @Test
    void should_remove_accents_and_normalize_characters() {
      // Act
      String result = slugService.generateSlug("Raquette de Ténnis Élite");

      // Assert
      assertEquals("raquette-de-tennis-elite", result);
    }

    @Test
    void should_remove_special_characters() {
      // Act
      String result = slugService.generateSlug("Produit @#$% Spécial !!!");

      // Assert
      assertEquals("produit-special", result);
    }

    @Test
    void should_collapse_multiple_dashes_into_one() {
      // Act
      String result = slugService.generateSlug("Produit --- super --- cool");

      // Assert
      assertEquals("produit-super-cool", result);
    }

    @Test
    void should_remove_leading_and_trailing_dashes() {
      // Act
      String result = slugService.generateSlug("---Produit Test---");

      // Assert
      assertEquals("produit-test", result);
    }

    @Test
    void should_cut_slug_to_max_length_of_80_characters() {
      // Arrange
      String longInput = "Produit très très très très très très très très très très très très long";

      // Act
      String result = slugService.generateSlug(longInput);

      // Assert
      assertTrue(result.length() <= 80);
    }

    @Test
    void should_not_end_with_dash_after_truncation() {
      // Arrange
      String input = "mot ".repeat(30);

      // Act
      String result = slugService.generateSlug(input);

      // Assert
      assertFalse(result.endsWith("-"));
    }

    @Test
    void should_handle_realistic_product_name() {
      // Act
      String result = slugService.generateSlug("Tennis Racket Pro 3000 - Édition 2025 !!!");

      // Assert
      assertEquals("tennis-racket-pro-3000-edition-2025", result);
    }
  }
}
