package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment_with_all_fields() {
    Comment comment = new Comment("Test body", "user-id", "article-id");

    assertNotNull(comment.getId());
    assertEquals("Test body", comment.getBody());
    assertEquals("user-id", comment.getUserId());
    assertEquals("article-id", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  public void should_have_unique_ids() {
    Comment comment1 = new Comment("Body 1", "user-id", "article-id");
    Comment comment2 = new Comment("Body 2", "user-id", "article-id");

    assertNotEquals(comment1.getId(), comment2.getId());
  }

  @Test
  public void should_be_equal_when_same_id() {
    Comment comment1 = new Comment("Test body", "user-id", "article-id");
    Comment comment2 = comment1;

    assertEquals(comment1, comment2);
  }

  @Test
  public void should_not_be_equal_when_different_id() {
    Comment comment1 = new Comment("Test body", "user-id", "article-id");
    Comment comment2 = new Comment("Test body", "user-id", "article-id");

    assertNotEquals(comment1, comment2);
  }

  @Test
  public void should_have_same_hashcode_when_equal() {
    Comment comment = new Comment("Test body", "user-id", "article-id");

    assertEquals(comment.hashCode(), comment.hashCode());
  }
}
