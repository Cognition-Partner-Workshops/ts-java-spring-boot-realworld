package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  void constructor_and_getters() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-id", 10);

    assertEquals("article-id", count.getId());
    assertEquals(10, count.getCount());
  }

  @Test
  void equals_sameValues() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("id", 5);

    assertEquals(count1, count2);
    assertEquals(count1.hashCode(), count2.hashCode());
  }

  @Test
  void notEquals_differentId() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("id1", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("id2", 5);

    assertNotEquals(count1, count2);
  }

  @Test
  void notEquals_differentCount() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("id", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("id", 10);

    assertNotEquals(count1, count2);
  }

  @Test
  void toString_notNull() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("id", 5);
    assertNotNull(count.toString());
    assertTrue(count.toString().contains("id"));
    assertTrue(count.toString().contains("5"));
  }
}
