package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PageTest {

  @Test
  void constructor_withValidParams() {
    Page page = new Page(10, 20);

    assertEquals(10, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void constructor_withNegativeOffset() {
    Page page = new Page(-5, 20);

    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void constructor_withZeroOffset() {
    Page page = new Page(0, 20);

    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void constructor_withLimitExceedingMax() {
    Page page = new Page(0, 200);

    assertEquals(0, page.getOffset());
    assertEquals(100, page.getLimit());
  }

  @Test
  void constructor_withNegativeLimit() {
    Page page = new Page(0, -5);

    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void constructor_withZeroLimit() {
    Page page = new Page(0, 0);

    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void noArgsConstructor() {
    Page page = new Page();

    assertEquals(0, page.getOffset());
    assertEquals(20, page.getLimit());
  }

  @Test
  void equals_sameValues() {
    Page page1 = new Page(10, 20);
    Page page2 = new Page(10, 20);

    assertEquals(page1, page2);
    assertEquals(page1.hashCode(), page2.hashCode());
  }

  @Test
  void notEquals_differentOffset() {
    Page page1 = new Page(10, 20);
    Page page2 = new Page(5, 20);

    assertNotEquals(page1, page2);
  }

  @Test
  void notEquals_differentLimit() {
    Page page1 = new Page(10, 20);
    Page page2 = new Page(10, 30);

    assertNotEquals(page1, page2);
  }

  @Test
  void toString_notNull() {
    Page page = new Page(10, 20);
    assertNotNull(page.toString());
  }
}
