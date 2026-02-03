package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  void constructor_withParams() {
    Comment comment = new Comment("Test body", "user-id", "article-id");

    assertNotNull(comment.getId());
    assertEquals("Test body", comment.getBody());
    assertEquals("user-id", comment.getUserId());
    assertEquals("article-id", comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  void noArgsConstructor() {
    Comment comment = new Comment();

    assertNull(comment.getId());
    assertNull(comment.getBody());
    assertNull(comment.getUserId());
    assertNull(comment.getArticleId());
    assertNull(comment.getCreatedAt());
  }

  @Test
  void equals_sameId() {
    Comment comment1 = new Comment("body1", "user1", "article1");
    Comment comment2 = new Comment("body2", "user2", "article2");

    assertNotEquals(comment1, comment2);
  }

  @Test
  void equals_sameComment() {
    Comment comment = new Comment("body", "user", "article");

    assertEquals(comment, comment);
  }

  @Test
  void hashCode_consistent() {
    Comment comment = new Comment("body", "user", "article");
    int hash1 = comment.hashCode();
    int hash2 = comment.hashCode();

    assertEquals(hash1, hash2);
  }
}
