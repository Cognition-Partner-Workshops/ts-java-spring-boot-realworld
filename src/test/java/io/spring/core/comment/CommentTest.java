package io.spring.core.comment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class CommentTest {

  @Test
  public void should_create_comment_with_all_fields() {
    Comment comment = new Comment("This is a comment", "user-123", "article-456");
    
    assertThat(comment.getId(), is(notNullValue()));
    assertThat(comment.getBody(), is("This is a comment"));
    assertThat(comment.getUserId(), is("user-123"));
    assertThat(comment.getArticleId(), is("article-456"));
    assertThat(comment.getCreatedAt(), is(notNullValue()));
  }

  @Test
  public void should_have_unique_id_for_each_comment() {
    Comment comment1 = new Comment("Comment 1", "user-1", "article-1");
    Comment comment2 = new Comment("Comment 2", "user-2", "article-2");
    
    assertThat(comment1.getId().equals(comment2.getId()), is(false));
  }

  @Test
  public void should_store_body_correctly() {
    String body = "This is a long comment with special characters: @#$%^&*()";
    Comment comment = new Comment(body, "user-1", "article-1");
    
    assertThat(comment.getBody(), is(body));
  }
}
