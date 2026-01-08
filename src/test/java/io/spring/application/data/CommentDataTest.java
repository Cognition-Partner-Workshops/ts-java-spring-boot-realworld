package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.application.DateTimeCursor;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CommentDataTest {

  @Test
  public void should_create_comment_data_with_all_args_constructor() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("id1", "author", "bio", "image", false);
    CommentData commentData = new CommentData(
        "id123", "Comment body", "article123", now, now, profileData);
    
    assertThat(commentData.getId(), is("id123"));
    assertThat(commentData.getBody(), is("Comment body"));
    assertThat(commentData.getArticleId(), is("article123"));
    assertThat(commentData.getCreatedAt(), is(now));
    assertThat(commentData.getUpdatedAt(), is(now));
    assertThat(commentData.getProfileData(), is(profileData));
  }

  @Test
  public void should_create_comment_data_with_no_args_constructor() {
    CommentData commentData = new CommentData();
    
    assertThat(commentData.getId(), nullValue());
    assertThat(commentData.getBody(), nullValue());
    assertThat(commentData.getArticleId(), nullValue());
    assertThat(commentData.getCreatedAt(), nullValue());
    assertThat(commentData.getUpdatedAt(), nullValue());
    assertThat(commentData.getProfileData(), nullValue());
  }

  @Test
  public void should_set_fields_via_setters() {
    CommentData commentData = new CommentData();
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("id1", "author", "bio", "image", false);
    
    commentData.setId("id123");
    commentData.setBody("Comment body");
    commentData.setArticleId("article123");
    commentData.setCreatedAt(now);
    commentData.setUpdatedAt(now);
    commentData.setProfileData(profileData);
    
    assertThat(commentData.getId(), is("id123"));
    assertThat(commentData.getBody(), is("Comment body"));
    assertThat(commentData.getArticleId(), is("article123"));
    assertThat(commentData.getCreatedAt(), is(now));
    assertThat(commentData.getUpdatedAt(), is(now));
    assertThat(commentData.getProfileData(), is(profileData));
  }

  @Test
  public void should_return_cursor_based_on_created_at() {
    DateTime now = DateTime.now();
    CommentData commentData = new CommentData();
    commentData.setCreatedAt(now);
    
    DateTimeCursor cursor = commentData.getCursor();
    
    assertThat(cursor, notNullValue());
  }

  @Test
  public void should_have_equals_based_on_all_fields() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("id1", "author", "bio", "image", false);
    CommentData commentData1 = new CommentData(
        "id123", "Comment body", "article123", now, now, profileData);
    CommentData commentData2 = new CommentData(
        "id123", "Comment body", "article123", now, now, profileData);
    CommentData commentData3 = new CommentData(
        "id456", "Comment body", "article123", now, now, profileData);
    
    assertThat(commentData1.equals(commentData2), is(true));
    assertThat(commentData1.equals(commentData3), is(false));
  }

  @Test
  public void should_have_hashcode_based_on_all_fields() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("id1", "author", "bio", "image", false);
    CommentData commentData1 = new CommentData(
        "id123", "Comment body", "article123", now, now, profileData);
    CommentData commentData2 = new CommentData(
        "id123", "Comment body", "article123", now, now, profileData);
    
    assertThat(commentData1.hashCode(), is(commentData2.hashCode()));
  }

  @Test
  public void should_have_toString() {
    CommentData commentData = new CommentData();
    commentData.setId("id123");
    commentData.setBody("Comment body");
    String toString = commentData.toString();
    
    assertThat(toString.contains("id123"), is(true));
    assertThat(toString.contains("Comment body"), is(true));
  }

  @Test
  public void should_not_equal_null() {
    CommentData commentData = new CommentData();
    assertThat(commentData.equals(null), is(false));
  }

  @Test
  public void should_not_equal_different_type() {
    CommentData commentData = new CommentData();
    assertThat(commentData.equals("string"), is(false));
  }
}
