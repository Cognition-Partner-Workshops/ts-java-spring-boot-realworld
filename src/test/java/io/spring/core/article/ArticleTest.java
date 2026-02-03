package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
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
    Article article = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList("java", "spring"), "user-id");

    assertNotNull(article.getId());
    assertEquals("test-title", article.getSlug());
    assertEquals("Test Title", article.getTitle());
    assertEquals("Test Description", article.getDescription());
    assertEquals("Test Body", article.getBody());
    assertEquals("user-id", article.getUserId());
    assertEquals(2, article.getTags().size());
    assertNotNull(article.getCreatedAt());
    assertNotNull(article.getUpdatedAt());
  }

  @Test
  public void should_create_article_with_empty_tags() {
    Article article = new Article("Test Title", "Test Description", "Test Body", 
        Collections.emptyList(), "user-id");

    assertNotNull(article.getId());
    assertTrue(article.getTags().isEmpty());
  }

  @Test
  public void should_update_article_title() {
    Article article = new Article("Original Title", "Description", "Body", 
        Collections.emptyList(), "user-id");
    
    article.update("New Title", null, null);

    assertEquals("New Title", article.getTitle());
    assertEquals("new-title", article.getSlug());
  }

  @Test
  public void should_update_article_description() {
    Article article = new Article("Title", "Original Description", "Body", 
        Collections.emptyList(), "user-id");
    
    article.update(null, "New Description", null);

    assertEquals("New Description", article.getDescription());
  }

  @Test
  public void should_update_article_body() {
    Article article = new Article("Title", "Description", "Original Body", 
        Collections.emptyList(), "user-id");
    
    article.update(null, null, "New Body");

    assertEquals("New Body", article.getBody());
  }

  @Test
  public void should_update_all_fields() {
    Article article = new Article("Title", "Description", "Body", 
        Collections.emptyList(), "user-id");
    
    article.update("New Title", "New Description", "New Body");

    assertEquals("New Title", article.getTitle());
    assertEquals("new-title", article.getSlug());
    assertEquals("New Description", article.getDescription());
    assertEquals("New Body", article.getBody());
  }

  @Test
  public void should_not_update_with_empty_values() {
    Article article = new Article("Title", "Description", "Body", 
        Collections.emptyList(), "user-id");
    
    article.update("", "", "");

    assertEquals("Title", article.getTitle());
    assertEquals("Description", article.getDescription());
    assertEquals("Body", article.getBody());
  }

  @Test
  public void should_have_unique_ids() {
    Article article1 = new Article("Title", "Description", "Body", 
        Collections.emptyList(), "user-id");
    Article article2 = new Article("Title", "Description", "Body", 
        Collections.emptyList(), "user-id");

    assertNotEquals(article1.getId(), article2.getId());
  }

  @Test
  public void should_deduplicate_tags() {
    Article article = new Article("Title", "Description", "Body", 
        Arrays.asList("java", "java", "spring"), "user-id");

    assertEquals(2, article.getTags().size());
  }
}
