package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PageTest {

  @Test
  void constructor_withValidParameters() {
    Page page = new Page(10, 20);

    assertEquals(10, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void constructor_withLimitExceedingMax() {
    Page page = new Page(0, 200);

    assertEquals(100, page.getLimit());
  }

  @Test
  void constructor_withZeroLimit() {
    Page page = new Page(0, 0);

    assertEquals(20, page.getLimit());
  }

  @Test
  void constructor_withNegativeLimit() {
    Page page = new Page(0, -5);

    assertEquals(20, page.getLimit());
  }

  @Test
  void constructor_withNegativeOffset() {
    Page page = new Page(-5, 10);

    assertEquals(0, page.getOffset());
  }

  @Test
  void constructor_withZeroOffset() {
    Page page = new Page(0, 10);

    assertEquals(0, page.getOffset());
  }

  @Test
  void noArgsConstructor_setsDefaults() {
    Page page = new Page();

    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }
}
