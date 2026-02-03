package io.spring.core.favorite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void should_create_article_favorite() {
    ArticleFavorite favorite = new ArticleFavorite("article-123", "user-456");
    
    assertThat(favorite.getArticleId(), is("article-123"));
    assertThat(favorite.getUserId(), is("user-456"));
  }

  @Test
  public void should_have_equals_based_on_article_and_user() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite favorite2 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite favorite3 = new ArticleFavorite("article-1", "user-2");
    
    assertThat(favorite1.equals(favorite2), is(true));
    assertThat(favorite1.equals(favorite3), is(false));
  }
}
