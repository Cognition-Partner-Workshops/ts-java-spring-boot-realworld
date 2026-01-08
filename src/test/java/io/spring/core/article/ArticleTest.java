package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
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
    Article article = new Article("Test Title", "Test Description", "Test Body", Arrays.asList("java", "spring"), "user123");
    
    assertThat(article.getTitle(), is("Test Title"));
    assertThat(article.getDescription(), is("Test Description"));
    assertThat(article.getBody(), is("Test Body"));
    assertThat(article.getUserId(), is("user123"));
  }

  @Test
  public void should_generate_uuid_for_id() {
    Article article = new Article("Test Title", "desc", "body", Arrays.asList("java"), "123");
    
    assertThat(article.getId(), notNullValue());
    assertThat(article.getId().length(), is(36));
  }

  @Test
  public void should_generate_unique_ids_for_different_articles() {
    Article article1 = new Article("Title 1", "desc1", "body1", Arrays.asList("java"), "123");
    Article article2 = new Article("Title 2", "desc2", "body2", Arrays.asList("spring"), "456");
    
    assertThat(article1.getId(), not(article2.getId()));
  }

  @Test
  public void should_set_created_at_timestamp() {
    Article article = new Article("Test Title", "desc", "body", Arrays.asList("java"), "123");
    
    assertThat(article.getCreatedAt(), notNullValue());
  }

  @Test
  public void should_set_updated_at_equal_to_created_at_initially() {
    Article article = new Article("Test Title", "desc", "body", Arrays.asList("java"), "123");
    
    assertThat(article.getUpdatedAt(), is(article.getCreatedAt()));
  }

  @Test
  public void should_create_article_with_custom_created_at() {
    DateTime customDate = new DateTime(2024, 1, 15, 10, 30);
    Article article = new Article("Test Title", "desc", "body", Arrays.asList("java"), "123", customDate);
    
    assertThat(article.getCreatedAt(), is(customDate));
    assertThat(article.getUpdatedAt(), is(customDate));
  }

  @Test
  public void should_create_tags_from_tag_list() {
    Article article = new Article("Test Title", "desc", "body", Arrays.asList("java", "spring", "testing"), "123");
    
    assertThat(article.getTags().size(), is(3));
  }

  @Test
  public void should_deduplicate_tags() {
    Article article = new Article("Test Title", "desc", "body", Arrays.asList("java", "java", "spring"), "123");
    
    assertThat(article.getTags().size(), is(2));
  }

  @Test
  public void should_handle_empty_tag_list() {
    Article article = new Article("Test Title", "desc", "body", Collections.emptyList(), "123");
    
    assertThat(article.getTags().size(), is(0));
  }

  @Test
  public void should_update_title_when_not_empty() {
    Article article = new Article("Old Title", "desc", "body", Arrays.asList("java"), "123");
    
    article.update("New Title", null, null);
    
    assertThat(article.getTitle(), is("New Title"));
    assertThat(article.getSlug(), is("new-title"));
  }

  @Test
  public void should_not_update_title_when_empty() {
    Article article = new Article("Old Title", "desc", "body", Arrays.asList("java"), "123");
    
    article.update("", null, null);
    
    assertThat(article.getTitle(), is("Old Title"));
  }

  @Test
  public void should_not_update_title_when_null() {
    Article article = new Article("Old Title", "desc", "body", Arrays.asList("java"), "123");
    
    article.update(null, null, null);
    
    assertThat(article.getTitle(), is("Old Title"));
  }

  @Test
  public void should_update_description_when_not_empty() {
    Article article = new Article("Title", "Old Description", "body", Arrays.asList("java"), "123");
    
    article.update(null, "New Description", null);
    
    assertThat(article.getDescription(), is("New Description"));
  }

  @Test
  public void should_not_update_description_when_empty() {
    Article article = new Article("Title", "Old Description", "body", Arrays.asList("java"), "123");
    
    article.update(null, "", null);
    
    assertThat(article.getDescription(), is("Old Description"));
  }

  @Test
  public void should_update_body_when_not_empty() {
    Article article = new Article("Title", "desc", "Old Body", Arrays.asList("java"), "123");
    
    article.update(null, null, "New Body");
    
    assertThat(article.getBody(), is("New Body"));
  }

  @Test
  public void should_not_update_body_when_empty() {
    Article article = new Article("Title", "desc", "Old Body", Arrays.asList("java"), "123");
    
    article.update(null, null, "");
    
    assertThat(article.getBody(), is("Old Body"));
  }

  @Test
  public void should_update_multiple_fields_at_once() {
    Article article = new Article("Old Title", "Old Description", "Old Body", Arrays.asList("java"), "123");
    
    article.update("New Title", "New Description", "New Body");
    
    assertThat(article.getTitle(), is("New Title"));
    assertThat(article.getDescription(), is("New Description"));
    assertThat(article.getBody(), is("New Body"));
  }

  @Test
  public void should_update_updated_at_when_title_changes() {
    DateTime originalDate = new DateTime(2024, 1, 1, 0, 0);
    Article article = new Article("Old Title", "desc", "body", Arrays.asList("java"), "123", originalDate);
    
    article.update("New Title", null, null);
    
    assertThat(article.getUpdatedAt(), not(originalDate));
  }

  @Test
  public void should_preserve_id_after_update() {
    Article article = new Article("Old Title", "desc", "body", Arrays.asList("java"), "123");
    String originalId = article.getId();
    
    article.update("New Title", "New Description", "New Body");
    
    assertThat(article.getId(), is(originalId));
  }

  @Test
  public void should_preserve_user_id_after_update() {
    Article article = new Article("Old Title", "desc", "body", Arrays.asList("java"), "user123");
    
    article.update("New Title", "New Description", "New Body");
    
    assertThat(article.getUserId(), is("user123"));
  }

  @Test
  public void should_preserve_tags_after_update() {
    Article article = new Article("Old Title", "desc", "body", Arrays.asList("java", "spring"), "123");
    int originalTagCount = article.getTags().size();
    
    article.update("New Title", "New Description", "New Body");
    
    assertThat(article.getTags().size(), is(originalTagCount));
  }

  @Test
  public void should_handle_special_characters_in_slug() {
    Article article = new Article("Hello & World", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("hello-world"));
  }

  @Test
  public void should_handle_quotes_in_slug() {
    Article article = new Article("It's a \"test\"", "desc", "body", Arrays.asList("java"), "123");
    assertThat(article.getSlug(), is("it's-a-\"test\""));
  }

  @Test
  public void should_convert_static_toSlug_method() {
    String slug = Article.toSlug("Test Title Here");
    assertThat(slug, is("test-title-here"));
  }
}
