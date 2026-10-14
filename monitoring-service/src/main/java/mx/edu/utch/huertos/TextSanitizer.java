package mx.edu.utch.huertos;

import java.text.Normalizer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public final class TextSanitizer {
  private TextSanitizer() {}

  public static String clean(String text) {
    if (text == null) return null;
    return Normalizer.normalize(Jsoup.clean(text, Safelist.none()), Normalizer.Form.NFKC)
        .replaceAll("[\\p{Cc}\\p{Cf}]", " ")
        .replaceAll("[\\s\\p{Z}]+", " ")
        .strip();
  }
}
