package io.spring.core.favorite;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void should_create_article_favorite_with_all_fields() {
    String articleId = "article-123";
    String userId = "user-456";

    ArticleFavorite favorite = new ArticleFavorite(articleId, userId);

    assertEquals(articleId, favorite.getArticleId());
    assertEquals(userId, favorite.getUserId());
  }

  @Test
  public void should_have_equals_based_on_all_fields() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite favorite2 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite favorite3 = new ArticleFavorite("article-2", "user-1");
    ArticleFavorite favorite4 = new ArticleFavorite("article-1", "user-2");

    assertEquals(favorite1, favorite2);
    assertNotEquals(favorite1, favorite3);
    assertNotEquals(favorite1, favorite4);
  }

  @Test
  public void should_have_consistent_hashcode() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite favorite2 = new ArticleFavorite("article-1", "user-1");

    assertEquals(favorite1.hashCode(), favorite2.hashCode());
  }

  @Test
  public void should_have_different_hashcode_for_different_favorites() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite favorite2 = new ArticleFavorite("article-2", "user-2");

    assertNotEquals(favorite1.hashCode(), favorite2.hashCode());
  }

  @Test
  public void should_store_article_id_correctly() {
    String articleId = "specific-article-id-12345";
    ArticleFavorite favorite = new ArticleFavorite(articleId, "user-1");

    assertEquals(articleId, favorite.getArticleId());
  }

  @Test
  public void should_store_user_id_correctly() {
    String userId = "specific-user-id-67890";
    ArticleFavorite favorite = new ArticleFavorite("article-1", userId);

    assertEquals(userId, favorite.getUserId());
  }

  @Test
  public void should_not_equal_null() {
    ArticleFavorite favorite = new ArticleFavorite("article-1", "user-1");

    assertNotEquals(null, favorite);
  }

  @Test
  public void should_equal_itself() {
    ArticleFavorite favorite = new ArticleFavorite("article-1", "user-1");

    assertEquals(favorite, favorite);
  }
}
