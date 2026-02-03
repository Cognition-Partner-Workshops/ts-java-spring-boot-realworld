package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment_with_all_fields() {
    Comment comment = new Comment("This is a comment body", "user123", "article456");

    assertNotNull(comment.getId());
    assertEquals("This is a comment body", comment.getBody());
    assertEquals("user123", comment.getUserId());
    assertEquals("article456", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  public void should_generate_unique_id_for_each_comment() {
    Comment comment1 = new Comment("Comment 1", "user1", "article1");
    Comment comment2 = new Comment("Comment 2", "user2", "article2");

    assertNotEquals(comment1.getId(), comment2.getId());
  }

  @Test
  public void should_set_created_at_timestamp() {
    Comment comment = new Comment("Test comment", "user123", "article456");

    assertNotNull(comment.getCreatedAt());
  }

  @Test
  public void should_have_equality_based_on_id() {
    Comment comment1 = new Comment("Same body", "user1", "article1");
    Comment comment2 = new Comment("Same body", "user1", "article1");

    assertNotEquals(comment1, comment2);
    assertEquals(comment1, comment1);
  }

  @Test
  public void should_create_comment_with_empty_body() {
    Comment comment = new Comment("", "user123", "article456");

    assertEquals("", comment.getBody());
    assertNotNull(comment.getId());
  }

  @Test
  public void should_create_comment_with_long_body() {
    String longBody = "A".repeat(1000);
    Comment comment = new Comment(longBody, "user123", "article456");

    assertEquals(longBody, comment.getBody());
    assertEquals(1000, comment.getBody().length());
  }
}
