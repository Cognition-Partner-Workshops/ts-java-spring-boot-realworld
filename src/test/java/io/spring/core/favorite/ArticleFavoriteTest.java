package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  void constructor_withParams() {
    ArticleFavorite favorite = new ArticleFavorite("article-id", "user-id");

    assertEquals("article-id", favorite.getArticleId());
    assertEquals("user-id", favorite.getUserId());
  }

  @Test
  void noArgsConstructor() {
    ArticleFavorite favorite = new ArticleFavorite();

    assertNull(favorite.getArticleId());
    assertNull(favorite.getUserId());
  }

  @Test
  void equals_sameValues() {
    ArticleFavorite favorite1 = new ArticleFavorite("article", "user");
    ArticleFavorite favorite2 = new ArticleFavorite("article", "user");

    assertEquals(favorite1, favorite2);
    assertEquals(favorite1.hashCode(), favorite2.hashCode());
  }

  @Test
  void notEquals_differentArticleId() {
    ArticleFavorite favorite1 = new ArticleFavorite("article1", "user");
    ArticleFavorite favorite2 = new ArticleFavorite("article2", "user");

    assertNotEquals(favorite1, favorite2);
  }

  @Test
  void notEquals_differentUserId() {
    ArticleFavorite favorite1 = new ArticleFavorite("article", "user1");
    ArticleFavorite favorite2 = new ArticleFavorite("article", "user2");

    assertNotEquals(favorite1, favorite2);
  }
}
