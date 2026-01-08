package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  public void should_create_article_favorite_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article123", 5);
    
    assertThat(count.getId(), is("article123"));
    assertThat(count.getCount(), is(5));
  }

  @Test
  public void should_create_article_favorite_count_with_zero_count() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article123", 0);
    
    assertThat(count.getId(), is("article123"));
    assertThat(count.getCount(), is(0));
  }

  @Test
  public void should_have_equals_based_on_all_fields() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("article123", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("article123", 5);
    ArticleFavoriteCount count3 = new ArticleFavoriteCount("article456", 5);
    ArticleFavoriteCount count4 = new ArticleFavoriteCount("article123", 10);
    
    assertThat(count1.equals(count2), is(true));
    assertThat(count1.equals(count3), is(false));
    assertThat(count1.equals(count4), is(false));
  }

  @Test
  public void should_have_hashcode_based_on_all_fields() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("article123", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("article123", 5);
    
    assertThat(count1.hashCode(), is(count2.hashCode()));
  }

  @Test
  public void should_have_toString() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article123", 5);
    String toString = count.toString();
    
    assertThat(toString.contains("article123"), is(true));
    assertThat(toString.contains("5"), is(true));
  }

  @Test
  public void should_not_equal_null() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article123", 5);
    assertThat(count.equals(null), is(false));
  }

  @Test
  public void should_not_equal_different_type() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article123", 5);
    assertThat(count.equals("string"), is(false));
  }

  @Test
  public void should_have_different_hashcode_for_different_values() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("article123", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("article456", 10);
    
    assertThat(count1.hashCode(), not(count2.hashCode()));
  }
}
