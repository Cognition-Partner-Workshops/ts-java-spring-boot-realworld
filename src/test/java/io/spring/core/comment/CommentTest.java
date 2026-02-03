package io.spring.core.comment;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment_with_all_fields() {
    String body = "This is a test comment";
    String userId = "user-123";
    String articleId = "article-456";

    Comment comment = new Comment(body, userId, articleId);

    assertNotNull(comment.getId());
    assertEquals(body, comment.getBody());
    assertEquals(userId, comment.getUserId());
    assertEquals(articleId, comment.getArticleId());
    assertNotNull(comment.getCreatedAt());
  }

  @Test
  public void should_generate_unique_id_for_each_comment() {
    Comment comment1 = new Comment("Comment 1", "user-1", "article-1");
    Comment comment2 = new Comment("Comment 2", "user-2", "article-2");

    assertNotNull(comment1.getId());
    assertNotNull(comment2.getId());
    assertNotEquals(comment1.getId(), comment2.getId());
  }

  @Test
  public void should_set_created_at_timestamp() {
    Comment comment = new Comment("Test comment", "user-123", "article-456");

    assertNotNull(comment.getCreatedAt());
  }

  @Test
  public void should_have_equals_based_on_id() {
    Comment comment1 = new Comment("Same body", "user-1", "article-1");
    Comment comment2 = new Comment("Same body", "user-1", "article-1");

    assertNotEquals(comment1, comment2);
    assertEquals(comment1, comment1);
  }

  @Test
  public void should_have_consistent_hashcode() {
    Comment comment = new Comment("Test comment", "user-123", "article-456");

    int hashCode1 = comment.hashCode();
    int hashCode2 = comment.hashCode();

    assertEquals(hashCode1, hashCode2);
  }

  @Test
  public void should_create_comment_with_empty_body() {
    Comment comment = new Comment("", "user-123", "article-456");

    assertEquals("", comment.getBody());
    assertNotNull(comment.getId());
  }

  @Test
  public void should_store_user_id_correctly() {
    String userId = "specific-user-id-12345";
    Comment comment = new Comment("Test", userId, "article-1");

    assertEquals(userId, comment.getUserId());
  }

  @Test
  public void should_store_article_id_correctly() {
    String articleId = "specific-article-id-67890";
    Comment comment = new Comment("Test", "user-1", articleId);

    assertEquals(articleId, comment.getArticleId());
  }
}
