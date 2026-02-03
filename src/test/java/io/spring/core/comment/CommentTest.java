package io.spring.core.comment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
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

  @Test
  public void should_have_equals_based_on_id() {
    Comment comment1 = new Comment("Comment 1", "user-1", "article-1");
    Comment comment2 = new Comment("Comment 2", "user-2", "article-2");
    
    assertThat(comment1.equals(comment1), is(true));
    assertThat(comment1.equals(comment2), is(false));
  }

  @Test
  public void should_not_equal_null() {
    Comment comment = new Comment("Comment", "user-1", "article-1");
    
    assertThat(comment.equals(null), is(false));
  }

  @Test
  public void should_not_equal_different_type() {
    Comment comment = new Comment("Comment", "user-1", "article-1");
    
    assertThat(comment.equals("string"), is(false));
  }

  @Test
  public void should_have_hashcode_based_on_id() {
    Comment comment1 = new Comment("Comment 1", "user-1", "article-1");
    Comment comment2 = new Comment("Comment 2", "user-2", "article-2");
    
    assertThat(comment1.hashCode(), is(not(comment2.hashCode())));
  }

  @Test
  public void should_create_with_no_args_constructor() {
    Comment comment = new Comment();
    
    assertThat(comment, is(notNullValue()));
  }

  @Test
  public void should_have_consistent_hashcode() {
    Comment comment = new Comment("Comment", "user-1", "article-1");
    int hashCode1 = comment.hashCode();
    int hashCode2 = comment.hashCode();
    
    assertThat(hashCode1, is(hashCode2));
  }

  @Test
  public void should_handle_null_id_in_equals() {
    Comment comment1 = new Comment();
    Comment comment2 = new Comment();
    
    assertThat(comment1.equals(comment2), is(true));
  }

  @Test
  public void should_not_equal_when_one_id_null() {
    Comment comment1 = new Comment("Comment", "user-1", "article-1");
    Comment comment2 = new Comment();
    
    assertThat(comment1.equals(comment2), is(false));
  }

  @Test
  public void should_not_equal_when_other_id_null() {
    Comment comment1 = new Comment();
    Comment comment2 = new Comment("Comment", "user-1", "article-1");
    
    assertThat(comment1.equals(comment2), is(false));
  }
}
