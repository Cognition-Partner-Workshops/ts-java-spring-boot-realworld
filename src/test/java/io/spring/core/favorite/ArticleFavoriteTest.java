package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void should_create_article_favorite() {
    ArticleFavorite favorite = new ArticleFavorite("article-id", "user-id");

    assertEquals("article-id", favorite.getArticleId());
    assertEquals("user-id", favorite.getUserId());
  }

  @Test
  public void should_be_equal_when_same_article_and_user_ids() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-id", "user-id");
    ArticleFavorite favorite2 = new ArticleFavorite("article-id", "user-id");

    assertEquals(favorite1, favorite2);
  }

  @Test
  public void should_not_be_equal_when_different_article_ids() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-id-1", "user-id");
    ArticleFavorite favorite2 = new ArticleFavorite("article-id-2", "user-id");

    assertNotEquals(favorite1, favorite2);
  }

  @Test
  public void should_not_be_equal_when_different_user_ids() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-id", "user-id-1");
    ArticleFavorite favorite2 = new ArticleFavorite("article-id", "user-id-2");

    assertNotEquals(favorite1, favorite2);
  }

  @Test
  public void should_have_same_hashcode_when_equal() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-id", "user-id");
    ArticleFavorite favorite2 = new ArticleFavorite("article-id", "user-id");

    assertEquals(favorite1.hashCode(), favorite2.hashCode());
  }
}
