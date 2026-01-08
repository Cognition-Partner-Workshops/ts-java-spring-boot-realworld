package io.spring.core.favorite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteTest {

  @Test
  public void should_create_article_favorite_with_constructor() {
    ArticleFavorite favorite = new ArticleFavorite("article123", "user456");
    
    assertThat(favorite.getArticleId(), is("article123"));
    assertThat(favorite.getUserId(), is("user456"));
  }

  @Test
  public void should_create_article_favorite_with_no_arg_constructor() {
    ArticleFavorite favorite = new ArticleFavorite();
    assertThat(favorite.getArticleId(), is((String) null));
    assertThat(favorite.getUserId(), is((String) null));
  }

  @Test
  public void should_have_equals_based_on_all_fields() {
    ArticleFavorite favorite1 = new ArticleFavorite("article1", "user1");
    ArticleFavorite favorite2 = new ArticleFavorite("article1", "user1");
    ArticleFavorite favorite3 = new ArticleFavorite("article1", "user2");
    ArticleFavorite favorite4 = new ArticleFavorite("article2", "user1");
    
    assertThat(favorite1.equals(favorite2), is(true));
    assertThat(favorite1.equals(favorite3), is(false));
    assertThat(favorite1.equals(favorite4), is(false));
  }

  @Test
  public void should_have_hashcode_based_on_all_fields() {
    ArticleFavorite favorite1 = new ArticleFavorite("article1", "user1");
    ArticleFavorite favorite2 = new ArticleFavorite("article1", "user1");
    ArticleFavorite favorite3 = new ArticleFavorite("article1", "user2");
    
    assertThat(favorite1.hashCode(), is(favorite2.hashCode()));
    assertThat(favorite1.hashCode() == favorite3.hashCode(), is(false));
  }

  @Test
  public void should_not_equal_null() {
    ArticleFavorite favorite = new ArticleFavorite("article1", "user1");
    assertThat(favorite.equals(null), is(false));
  }

  @Test
  public void should_not_equal_different_type() {
    ArticleFavorite favorite = new ArticleFavorite("article1", "user1");
    assertThat(favorite.equals("string"), is(false));
  }

  @Test
  public void should_equal_itself() {
    ArticleFavorite favorite = new ArticleFavorite("article1", "user1");
    assertThat(favorite.equals(favorite), is(true));
  }
}
