package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CommentTest {

  @Test
  void constructor_createsWithAllFields() {
    Comment comment = new Comment("Test body", "user-id", "article-id");

    assertNotNull(comment.getId());
    assertEquals("Test body", comment.getBody());
    assertEquals("user-id", comment.getUserId());
    assertEquals("article-id", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void noArgsConstructor_createsEmptyComment() {
    Comment comment = new Comment();

    assertNull(comment.getId());
    assertNull(comment.getBody());
    assertNull(comment.getUserId());
    assertNull(comment.getArticleId());
    assertNull(comment.getCreatedAt());
  }

  @Test
  void equals_basedOnId() {
    Comment comment1 = new Comment("Test body", "user-id", "article-id");
    Comment comment2 = new Comment("Different body", "user-id", "article-id");

    assertNotEquals(comment1, comment2);
  }

  @Test
  void equals_withSameObject() {
    Comment comment = new Comment("Test body", "user-id", "article-id");

    assertEquals(comment, comment);
  }

  @Test
  void equals_withNull() {
    Comment comment = new Comment("Test body", "user-id", "article-id");

    assertNotEquals(comment, null);
  }

  @Test
  void equals_withDifferentClass() {
    Comment comment = new Comment("Test body", "user-id", "article-id");

    assertNotEquals(comment, "string");
  }

  @Test
  void hashCode_consistentWithEquals() {
    Comment comment1 = new Comment("Test body", "user-id", "article-id");

    assertEquals(comment1.hashCode(), comment1.hashCode());
  }
}
