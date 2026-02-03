package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ArticleFavoriteCountTest {

  @Test
  public void should_create_article_favorite_count_with_all_args_constructor() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-id", 5);

    assertThat(count.getId(), is("article-id"));
    assertThat(count.getCount(), is(5));
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("article-id", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("article-id", 5);
    ArticleFavoriteCount count3 = new ArticleFavoriteCount("different-id", 5);

    assertThat(count1.equals(count2), is(true));
    assertThat(count1.equals(count3), is(false));
    assertThat(count1.hashCode(), is(count2.hashCode()));
    assertThat(count1.hashCode(), is(not(count3.hashCode())));
  }

  @Test
  public void should_implement_to_string() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-id", 5);

    String toString = count.toString();

    assertThat(toString, is(notNullValue()));
    assertThat(toString.contains("article-id"), is(true));
  }

  @Test
  public void should_handle_equals_with_null_and_different_type() {
    ArticleFavoriteCount count = new ArticleFavoriteCount("article-id", 5);

    assertThat(count.equals(null), is(false));
    assertThat(count.equals("string"), is(false));
    assertThat(count.equals(count), is(true));
  }

  @Test
  public void should_handle_different_count() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("article-id", 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("article-id", 10);

    assertThat(count1.equals(count2), is(false));
  }

  @Test
  public void should_handle_null_id() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount(null, 5);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount(null, 5);

    assertThat(count1.equals(count2), is(true));
    assertThat(count1.hashCode(), is(count2.hashCode()));
  }

  @Test
  public void should_handle_null_count() {
    ArticleFavoriteCount count1 = new ArticleFavoriteCount("article-id", null);
    ArticleFavoriteCount count2 = new ArticleFavoriteCount("article-id", null);

    assertThat(count1.equals(count2), is(true));
  }
}
