package io.spring.core.favorite;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
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

  @Test
  public void should_have_different_hashcode_for_different_favorites() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite favorite2 = new ArticleFavorite("article-2", "user-1");
    
    assertThat(favorite1.hashCode(), is(not(favorite2.hashCode())));
  }

  @Test
  public void should_have_same_hashcode_for_equal_favorites() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite favorite2 = new ArticleFavorite("article-1", "user-1");
    
    assertThat(favorite1.hashCode(), is(favorite2.hashCode()));
  }

  @Test
  public void should_not_equal_null() {
    ArticleFavorite favorite = new ArticleFavorite("article-1", "user-1");
    
    assertThat(favorite.equals(null), is(false));
  }

  @Test
  public void should_not_equal_different_type() {
    ArticleFavorite favorite = new ArticleFavorite("article-1", "user-1");
    
    assertThat(favorite.equals("string"), is(false));
  }

  @Test
  public void should_equal_itself() {
    ArticleFavorite favorite = new ArticleFavorite("article-1", "user-1");
    
    assertThat(favorite.equals(favorite), is(true));
  }

  @Test
  public void should_create_with_no_args_constructor() {
    ArticleFavorite favorite = new ArticleFavorite();
    
    assertThat(favorite, is(notNullValue()));
  }

  @Test
  public void should_not_equal_when_article_id_differs() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite favorite2 = new ArticleFavorite("article-2", "user-1");
    
    assertThat(favorite1.equals(favorite2), is(false));
  }

  @Test
  public void should_not_equal_when_user_id_differs() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite favorite2 = new ArticleFavorite("article-1", "user-2");
    
    assertThat(favorite1.equals(favorite2), is(false));
  }

  @Test
  public void should_handle_null_article_id() {
    ArticleFavorite favorite1 = new ArticleFavorite(null, "user-1");
    ArticleFavorite favorite2 = new ArticleFavorite(null, "user-1");
    
    assertThat(favorite1.equals(favorite2), is(true));
  }

  @Test
  public void should_handle_null_user_id() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-1", null);
    ArticleFavorite favorite2 = new ArticleFavorite("article-1", null);
    
    assertThat(favorite1.equals(favorite2), is(true));
  }

  @Test
  public void should_not_equal_when_one_article_id_null() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite favorite2 = new ArticleFavorite(null, "user-1");
    
    assertThat(favorite1.equals(favorite2), is(false));
  }

  @Test
  public void should_not_equal_when_one_user_id_null() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-1", "user-1");
    ArticleFavorite favorite2 = new ArticleFavorite("article-1", null);
    
    assertThat(favorite1.equals(favorite2), is(false));
  }

  @Test
  public void should_not_equal_when_other_article_id_null() {
    ArticleFavorite favorite1 = new ArticleFavorite(null, "user-1");
    ArticleFavorite favorite2 = new ArticleFavorite("article-1", "user-1");
    
    assertThat(favorite1.equals(favorite2), is(false));
  }

  @Test
  public void should_not_equal_when_other_user_id_null() {
    ArticleFavorite favorite1 = new ArticleFavorite("article-1", null);
    ArticleFavorite favorite2 = new ArticleFavorite("article-1", "user-1");
    
    assertThat(favorite1.equals(favorite2), is(false));
  }

  @Test
  public void should_handle_both_fields_null() {
    ArticleFavorite favorite1 = new ArticleFavorite(null, null);
    ArticleFavorite favorite2 = new ArticleFavorite(null, null);
    
    assertThat(favorite1.equals(favorite2), is(true));
  }
}
