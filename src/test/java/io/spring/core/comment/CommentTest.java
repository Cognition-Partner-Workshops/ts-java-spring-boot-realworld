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
    Comment comment1 = new Comment("body1", "user1", "article1");
    Comment comment2 = new Comment("body2", "user2", "article2");
    
    assertThat(comment1.getId(), not(comment2.getId()));
  }

  @Test
  public void should_have_equals_based_on_id() {
    Comment comment1 = new Comment("body", "user", "article");
    Comment comment2 = new Comment("body", "user", "article");
    
    assertThat(comment1.equals(comment2), is(false));
    assertThat(comment1.equals(comment1), is(true));
  }

  @Test
  public void should_have_hashcode_based_on_id() {
    Comment comment1 = new Comment("body", "user", "article");
    Comment comment2 = new Comment("body", "user", "article");
    
    assertThat(comment1.hashCode(), not(comment2.hashCode()));
    assertThat(comment1.hashCode(), is(comment1.hashCode()));
  }

  @Test
  public void should_create_comment_with_no_arg_constructor() {
    Comment comment = new Comment();
    assertThat(comment.getId(), is((String) null));
    assertThat(comment.getBody(), is((String) null));
    assertThat(comment.getUserId(), is((String) null));
    assertThat(comment.getArticleId(), is((String) null));
  }

  @Test
  public void should_not_equal_null() {
    Comment comment = new Comment("body", "user", "article");
    assertThat(comment.equals(null), is(false));
  }

  @Test
  public void should_not_equal_different_type() {
    Comment comment = new Comment("body", "user", "article");
    assertThat(comment.equals("string"), is(false));
  }

  @Test
  public void should_set_created_at_on_construction() {
    long before = System.currentTimeMillis();
    Comment comment = new Comment("body", "user", "article");
    long after = System.currentTimeMillis();
    
    assertThat(comment.getCreatedAt().getMillis() >= before, is(true));
    assertThat(comment.getCreatedAt().getMillis() <= after, is(true));
  }
}
