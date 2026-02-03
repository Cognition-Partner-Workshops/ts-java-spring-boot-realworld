package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ArticleFavoriteTest {

  @Test
  void constructor_createsWithAllFields() {
    ArticleFavorite favorite = new ArticleFavorite("article-id", "user-id");

    assertEquals("article-id", favorite.getArticleId());
    assertEquals("user-id", favorite.getUserId());
  }

  @Test
  void noArgsConstructor_createsEmptyObject() {
    ArticleFavorite favorite = new ArticleFavorite();

    assertNull(favorite.getArticleId());
    assertNull(favorite.getUserId());
  }

  @Test
  void equals_withSameData() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-id", "user-id");
    ArticleFavorite favorite2 = new ArticleFavorite("article-id", "user-id");

    assertEquals(favorite1, favorite2);
    assertEquals(favorite1.hashCode(), favorite2.hashCode());
  }

  @Test
  void equals_withDifferentArticleId() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-id-1", "user-id");
    ArticleFavorite favorite2 = new ArticleFavorite("article-id-2", "user-id");

    assertNotEquals(favorite1, favorite2);
  }

  @Test
  void equals_withDifferentUserId() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-id", "user-id-1");
    ArticleFavorite favorite2 = new ArticleFavorite("article-id", "user-id-2");

    assertNotEquals(favorite1, favorite2);
  }

  @Test
  void equals_withNull() {
    ArticleFavorite favorite = new ArticleFavorite("article-id", "user-id");

    assertNotEquals(favorite, null);
  }

  @Test
  void equals_withDifferentClass() {
    ArticleFavorite favorite = new ArticleFavorite("article-id", "user-id");

    assertNotEquals(favorite, "string");
  }

  @Test
  void equals_withSameObject() {
    ArticleFavorite favorite = new ArticleFavorite("article-id", "user-id");

    assertEquals(favorite, favorite);
  }
}
