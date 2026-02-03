package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.application.DateTimeCursor;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CommentDataTest {

  @Test
  public void should_create_comment_data_with_all_args_constructor() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    
    CommentData commentData = new CommentData(
        "id", "body", "article-id", now, now, profileData);

    assertThat(commentData.getId(), is("id"));
    assertThat(commentData.getBody(), is("body"));
    assertThat(commentData.getArticleId(), is("article-id"));
    assertThat(commentData.getCreatedAt(), is(now));
    assertThat(commentData.getUpdatedAt(), is(now));
    assertThat(commentData.getProfileData(), is(profileData));
  }

  @Test
  public void should_create_comment_data_with_no_args_constructor() {
    CommentData commentData = new CommentData();
    
    assertThat(commentData.getId(), is((String) null));
    assertThat(commentData.getBody(), is((String) null));
  }

  @Test
  public void should_set_and_get_properties() {
    CommentData commentData = new CommentData();
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    
    commentData.setId("id");
    commentData.setBody("body");
    commentData.setArticleId("article-id");
    commentData.setCreatedAt(now);
    commentData.setUpdatedAt(now);
    commentData.setProfileData(profileData);

    assertThat(commentData.getId(), is("id"));
    assertThat(commentData.getBody(), is("body"));
    assertThat(commentData.getArticleId(), is("article-id"));
    assertThat(commentData.getCreatedAt(), is(now));
    assertThat(commentData.getUpdatedAt(), is(now));
    assertThat(commentData.getProfileData(), is(profileData));
  }

  @Test
  public void should_get_cursor() {
    DateTime now = DateTime.now();
    CommentData commentData = new CommentData();
    commentData.setCreatedAt(now);

    DateTimeCursor cursor = commentData.getCursor();

    assertThat(cursor, is(notNullValue()));
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    
    CommentData commentData1 = new CommentData(
        "id", "body", "article-id", now, now, profileData);
    CommentData commentData2 = new CommentData(
        "id", "body", "article-id", now, now, profileData);
    CommentData commentData3 = new CommentData(
        "different-id", "body", "article-id", now, now, profileData);

    assertThat(commentData1.equals(commentData2), is(true));
    assertThat(commentData1.equals(commentData3), is(false));
    assertThat(commentData1.hashCode(), is(commentData2.hashCode()));
    assertThat(commentData1.hashCode(), is(not(commentData3.hashCode())));
  }

  @Test
  public void should_implement_to_string() {
    CommentData commentData = new CommentData();
    commentData.setId("id");
    commentData.setBody("body");

    String toString = commentData.toString();

    assertThat(toString, is(notNullValue()));
    assertThat(toString.contains("id"), is(true));
    assertThat(toString.contains("body"), is(true));
  }

  @Test
  public void should_handle_equals_with_null_and_different_type() {
    CommentData commentData = new CommentData();
    commentData.setId("id");

    assertThat(commentData.equals(null), is(false));
    assertThat(commentData.equals("string"), is(false));
    assertThat(commentData.equals(commentData), is(true));
  }

  @Test
  public void should_handle_equals_with_null_fields() {
    CommentData commentData1 = new CommentData();
    CommentData commentData2 = new CommentData();

    assertThat(commentData1.equals(commentData2), is(true));
    assertThat(commentData1.hashCode(), is(commentData2.hashCode()));
  }
}
