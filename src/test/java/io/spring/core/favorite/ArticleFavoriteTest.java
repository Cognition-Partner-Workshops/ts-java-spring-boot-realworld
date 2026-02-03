package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void should_create_article_favorite_with_article_and_user() {
    ArticleFavorite favorite = new ArticleFavorite("article123", "user456");

    assertEquals("article123", favorite.getArticleId());
    assertEquals("user456", favorite.getUserId());
  }

  @Test
  public void should_create_article_favorite_with_no_args_constructor() {
    ArticleFavorite favorite = new ArticleFavorite();

    assertNull(favorite.getArticleId());
    assertNull(favorite.getUserId());
  }

  @Test
  public void should_have_equality_based_on_all_fields() {
    ArticleFavorite favorite1 = new ArticleFavorite("article1", "user1");
    ArticleFavorite favorite2 = new ArticleFavorite("article1", "user1");
    ArticleFavorite favorite3 = new ArticleFavorite("article1", "user2");
    ArticleFavorite favorite4 = new ArticleFavorite("article2", "user1");

    assertEquals(favorite1, favorite2);
    assertNotEquals(favorite1, favorite3);
    assertNotEquals(favorite1, favorite4);
  }

  @Test
  public void should_have_consistent_hash_code() {
    ArticleFavorite favorite1 = new ArticleFavorite("article1", "user1");
    ArticleFavorite favorite2 = new ArticleFavorite("article1", "user1");

    assertEquals(favorite1.hashCode(), favorite2.hashCode());
  }

  @Test
  public void should_not_equal_null() {
    ArticleFavorite favorite = new ArticleFavorite("article1", "user1");

    assertNotEquals(null, favorite);
  }

  @Test
  public void should_not_equal_different_type() {
    ArticleFavorite favorite = new ArticleFavorite("article1", "user1");

    assertNotEquals("not a favorite", favorite);
  }
}
