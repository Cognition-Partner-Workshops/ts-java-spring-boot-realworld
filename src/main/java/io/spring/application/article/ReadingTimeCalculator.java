package io.spring.application.article;

import java.util.regex.Pattern;

public final class ReadingTimeCalculator {
  public static final int WORDS_PER_MINUTE = 200;

  private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>");
  private static final Pattern MARKDOWN_IMAGES = Pattern.compile("!\\[([^\\]]*)\\]\\([^)]*\\)");
  private static final Pattern MARKDOWN_LINKS = Pattern.compile("\\[([^\\]]*)\\]\\([^)]*\\)");
  private static final Pattern NON_WORD_CHARACTERS = Pattern.compile("[^\\p{L}\\p{N}'’]+");

  private ReadingTimeCalculator() {}

  public static int countWords(String markdown) {
    if (markdown == null || markdown.isBlank()) {
      return 0;
    }

    String text = HTML_TAGS.matcher(markdown).replaceAll(" ");
    text = MARKDOWN_IMAGES.matcher(text).replaceAll("$1");
    text = MARKDOWN_LINKS.matcher(text).replaceAll("$1");
    text = NON_WORD_CHARACTERS.matcher(text).replaceAll(" ").trim();
    if (text.isEmpty()) {
      return 0;
    }
    return text.split("\\s+").length;
  }

  public static int minutesFor(String markdown) {
    return Math.max(1, (int) Math.ceil(countWords(markdown) / (double) WORDS_PER_MINUTE));
  }
}
