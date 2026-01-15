package io.spring.core.comment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Comment Entity Tests")
public class CommentTest {

  @Test
  @DisplayName("should create comment with all fields")
  public void should_create_comment_with_all_fields() {
    Comment comment = new Comment("This is a test comment", "user-123", "article-456");
    
    assertThat(comment.getBody(), is("This is a test comment"));
    assertThat(comment.getUserId(), is("user-123"));
    assertThat(comment.getArticleId(), is("article-456"));
  }

  @Test
  @DisplayName("should generate unique id on creation")
  public void should_generate_unique_id_on_creation() {
    Comment comment1 = new Comment("Comment 1", "user-1", "article-1");
    Comment comment2 = new Comment("Comment 2", "user-2", "article-2");
    
    assertThat(comment1.getId(), notNullValue());
    assertThat(comment2.getId(), notNullValue());
    assertThat(comment1.getId(), not(is(comment2.getId())));
  }

  @Test
  @DisplayName("should set createdAt timestamp on creation")
  public void should_set_createdAt_timestamp_on_creation() {
    Comment comment = new Comment("Test comment", "user-123", "article-456");
    
    assertThat(comment.getCreatedAt(), notNullValue());
  }

  @Test
  @DisplayName("should handle empty body")
  public void should_handle_empty_body() {
    Comment comment = new Comment("", "user-123", "article-456");
    
    assertThat(comment.getBody(), is(""));
    assertThat(comment.getUserId(), is("user-123"));
    assertThat(comment.getArticleId(), is("article-456"));
  }

  @Test
  @DisplayName("should handle long comment body")
  public void should_handle_long_comment_body() {
    String longBody = "This is a very long comment. ".repeat(100);
    Comment comment = new Comment(longBody, "user-123", "article-456");
    
    assertThat(comment.getBody(), is(longBody));
  }

  @Test
  @DisplayName("should handle special characters in body")
  public void should_handle_special_characters_in_body() {
    String specialBody = "Comment with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
    Comment comment = new Comment(specialBody, "user-123", "article-456");
    
    assertThat(comment.getBody(), is(specialBody));
  }

  @Test
  @DisplayName("should handle unicode characters in body")
  public void should_handle_unicode_characters_in_body() {
    String unicodeBody = "Comment with unicode: 你好世界 こんにちは 안녕하세요";
    Comment comment = new Comment(unicodeBody, "user-123", "article-456");
    
    assertThat(comment.getBody(), is(unicodeBody));
  }

  @Test
  @DisplayName("should handle newlines in body")
  public void should_handle_newlines_in_body() {
    String multilineBody = "Line 1\nLine 2\nLine 3";
    Comment comment = new Comment(multilineBody, "user-123", "article-456");
    
    assertThat(comment.getBody(), is(multilineBody));
  }

  @Test
  @DisplayName("comments with same id should be equal")
  public void comments_with_same_id_should_be_equal() {
    Comment comment1 = new Comment("Comment 1", "user-1", "article-1");
    Comment comment2 = new Comment("Comment 2", "user-2", "article-2");
    
    assertThat(comment1.equals(comment1), is(true));
    assertThat(comment1.equals(comment2), is(false));
  }

  @Test
  @DisplayName("should preserve user id")
  public void should_preserve_user_id() {
    Comment comment = new Comment("Test comment", "specific-user-id", "article-456");
    
    assertThat(comment.getUserId(), is("specific-user-id"));
  }

  @Test
  @DisplayName("should preserve article id")
  public void should_preserve_article_id() {
    Comment comment = new Comment("Test comment", "user-123", "specific-article-id");
    
    assertThat(comment.getArticleId(), is("specific-article-id"));
  }

  @Test
  @DisplayName("multiple comments on same article should have different ids")
  public void multiple_comments_on_same_article_should_have_different_ids() {
    Comment comment1 = new Comment("First comment", "user-1", "article-123");
    Comment comment2 = new Comment("Second comment", "user-2", "article-123");
    Comment comment3 = new Comment("Third comment", "user-1", "article-123");
    
    assertThat(comment1.getId(), not(is(comment2.getId())));
    assertThat(comment2.getId(), not(is(comment3.getId())));
    assertThat(comment1.getId(), not(is(comment3.getId())));
  }

  @Test
  @DisplayName("same user can comment on different articles")
  public void same_user_can_comment_on_different_articles() {
    Comment comment1 = new Comment("Comment on article 1", "user-123", "article-1");
    Comment comment2 = new Comment("Comment on article 2", "user-123", "article-2");
    
    assertThat(comment1.getUserId(), is(comment2.getUserId()));
    assertThat(comment1.getArticleId(), not(is(comment2.getArticleId())));
  }
}
