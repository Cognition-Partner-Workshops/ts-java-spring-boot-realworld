package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ArticleTest {

  @Test
  public void should_get_right_slug() {
    Article article = new Article("a new   title", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title"));
  }

  @Test
  public void should_get_right_slug_with_number_in_title() {
    Article article = new Article("a new title 2", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title-2"));
  }

  @Test
  public void should_get_lower_case_slug() {
    Article article = new Article("A NEW TITLE", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("a-new-title"));
  }

  @Test
  public void should_handle_other_language() {
    Article article = new Article("中文：标题", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("中文-标题"));
  }

  @Test
  public void should_handle_commas() {
    Article article = new Article("what?the.hell,w", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("what-the-hell-w"));
  }

  @Test
  public void should_create_article_with_all_fields() {
    Article article = new Article("Test Title", "Test Description", "Test Body", Arrays.asList("java", "spring"), "user-123");
    assertThat(article.getTitle(), is("Test Title"));
    assertThat(article.getDescription(), is("Test Description"));
    assertThat(article.getBody(), is("Test Body"));
    assertThat(article.getUserId(), is("user-123"));
    assertThat(article.getTags().size(), is(2));
  }

  @Test
  public void should_update_title() {
    Article article = new Article("Old Title", "Desc", "Body", Arrays.asList("java"), "user-1");
    article.update("New Title", null, null);
    assertThat(article.getTitle(), is("New Title"));
    assertThat(article.getSlug(), is("new-title"));
  }

  @Test
  public void should_update_description() {
    Article article = new Article("Title", "Old Desc", "Body", Arrays.asList("java"), "user-1");
    article.update(null, "New Description", null);
    assertThat(article.getDescription(), is("New Description"));
  }

  @Test
  public void should_update_body() {
    Article article = new Article("Title", "Desc", "Old Body", Arrays.asList("java"), "user-1");
    article.update(null, null, "New Body");
    assertThat(article.getBody(), is("New Body"));
  }

  @Test
  public void should_update_all_fields() {
    Article article = new Article("Old Title", "Old Desc", "Old Body", Arrays.asList("java"), "user-1");
    article.update("New Title", "New Desc", "New Body");
    assertThat(article.getTitle(), is("New Title"));
    assertThat(article.getDescription(), is("New Desc"));
    assertThat(article.getBody(), is("New Body"));
  }

  @Test
  public void should_not_update_with_empty_values() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList("java"), "user-1");
    article.update("", "", "");
    assertThat(article.getTitle(), is("Title"));
    assertThat(article.getDescription(), is("Desc"));
    assertThat(article.getBody(), is("Body"));
  }

  @Test
  public void should_deduplicate_tags() {
    Article article = new Article("Title", "Desc", "Body", Arrays.asList("java", "java", "spring", "spring"), "user-1");
    assertThat(article.getTags().size(), is(2));
  }

  @Test
  public void should_generate_slug_statically() {
    assertThat(Article.toSlug("Hello World"), is("hello-world"));
    assertThat(Article.toSlug("Test Article"), is("test-article"));
  }
}
