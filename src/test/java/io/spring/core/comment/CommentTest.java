package io.spring.core.comment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment_with_all_fields() {
    Comment comment = new Comment("This is a comment body", "user123", "article456");
    
    assertThat(comment.getId(), notNullValue());
    assertThat(comment.getBody(), is("This is a comment body"));
    assertThat(comment.getUserId(), is("user123"));
    assertThat(comment.getArticleId(), is("article456"));
    assertThat(comment.getCreatedAt(), notNullValue());
  }

  @Test
  public void should_generate_unique_id_for_each_comment() {
    Comment comment1 = new Comment("Comment 1", "user1", "article1");
    Comment comment2 = new Comment("Comment 2", "user2", "article2");
    
    assertThat(comment1.getId(), not(comment2.getId()));
  }

  @Test
  public void should_set_created_at_timestamp() {
    Comment comment = new Comment("Test comment", "user123", "article456");
    
    assertThat(comment.getCreatedAt(), notNullValue());
  }

  @Test
  public void should_create_comment_with_empty_body() {
    Comment comment = new Comment("", "user123", "article456");
    
    assertThat(comment.getId(), notNullValue());
    assertThat(comment.getBody(), is(""));
  }

  @Test
  public void should_have_equality_based_on_id() {
    Comment comment1 = new Comment("Same body", "user123", "article456");
    Comment comment2 = new Comment("Same body", "user123", "article456");
    
    assertThat(comment1.equals(comment2), is(false));
    assertThat(comment1.equals(comment1), is(true));
  }

  @Test
  public void should_create_comment_with_long_body() {
    String longBody = "This is a very long comment body that contains multiple sentences. " +
        "It should be able to handle long text without any issues. " +
        "Comments can be quite lengthy in real-world applications.";
    Comment comment = new Comment(longBody, "user123", "article456");
    
    assertThat(comment.getBody(), is(longBody));
  }

  @Test
  public void should_create_comment_with_special_characters() {
    String bodyWithSpecialChars = "Comment with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";
    Comment comment = new Comment(bodyWithSpecialChars, "user123", "article456");
    
    assertThat(comment.getBody(), is(bodyWithSpecialChars));
  }

  @Test
  public void should_create_comment_with_unicode_characters() {
    String unicodeBody = "Comment with unicode: 你好世界 🌍 émojis";
    Comment comment = new Comment(unicodeBody, "user123", "article456");
    
    assertThat(comment.getBody(), is(unicodeBody));
  }
}
