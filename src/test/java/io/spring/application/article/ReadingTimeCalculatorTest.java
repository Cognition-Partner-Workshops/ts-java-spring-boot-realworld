package io.spring.application.article;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ReadingTimeCalculatorTest {

  @Test
  void null_and_blank_text_have_minimum_reading_time() {
    assertThat(ReadingTimeCalculator.countWords(null)).isZero();
    assertThat(ReadingTimeCalculator.countWords(" \n\t ")).isZero();
    assertThat(ReadingTimeCalculator.minutesFor(null)).isEqualTo(1);
    assertThat(ReadingTimeCalculator.minutesFor(" \n\t ")).isEqualTo(1);
  }

  @Test
  void calculates_minutes_from_word_count() {
    assertThat(ReadingTimeCalculator.minutesFor(words(200))).isEqualTo(1);
    assertThat(ReadingTimeCalculator.minutesFor(words(201))).isEqualTo(2);
    assertThat(ReadingTimeCalculator.minutesFor(words(1000))).isEqualTo(5);
  }

  @Test
  void counts_markdown_text_but_not_syntax_or_link_urls() {
    String markdown =
        "# Title\n\n**bold** text with [a link](http://example.com/very/long/path)"
            + " ![alt text](img.png)";

    assertThat(ReadingTimeCalculator.countWords(markdown)).isEqualTo(8);
  }

  @Test
  void strips_html_tags() {
    assertThat(ReadingTimeCalculator.countWords("<p>one</p><br/>two")).isEqualTo(2);
  }

  @Test
  void counts_contractions_and_unicode_words() {
    assertThat(ReadingTimeCalculator.countWords("don't")).isEqualTo(1);
    assertThat(ReadingTimeCalculator.countWords("café naïve")).isEqualTo(2);
  }

  private String words(int count) {
    return String.join(" ", java.util.Collections.nCopies(count, "word"));
  }
}
