package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class CommentDataTest {

  @Test
  void constructor_and_getters() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image", false);
    CommentData commentData =
        new CommentData("comment-id", "Test body", "article-id", now, now, profileData);

    assertEquals("comment-id", commentData.getId());
    assertEquals("Test body", commentData.getBody());
    assertEquals("article-id", commentData.getArticleId());
    assertEquals(now, commentData.getCreatedAt());
    assertEquals(now, commentData.getUpdatedAt());
    assertEquals(profileData, commentData.getProfileData());
  }

  @Test
  void getCursor_returnsDateTimeCursor() {
    DateTime createdAt = DateTime.now();
    CommentData commentData =
        new CommentData("id", "body", "article-id", createdAt, DateTime.now(), null);

    DateTimeCursor cursor = commentData.getCursor();

    assertNotNull(cursor);
    assertEquals(createdAt, cursor.getData());
  }

  @Test
  void setters() {
    CommentData commentData = new CommentData();
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image", false);

    commentData.setId("new-id");
    commentData.setBody("new body");
    commentData.setArticleId("new-article-id");
    commentData.setCreatedAt(now);
    commentData.setUpdatedAt(now);
    commentData.setProfileData(profileData);

    assertEquals("new-id", commentData.getId());
    assertEquals("new body", commentData.getBody());
    assertEquals("new-article-id", commentData.getArticleId());
    assertEquals(now, commentData.getCreatedAt());
    assertEquals(now, commentData.getUpdatedAt());
    assertEquals(profileData, commentData.getProfileData());
  }

  @Test
  void equals_and_hashCode() {
    DateTime now = DateTime.now();
    CommentData commentData1 = new CommentData("id", "body", "article-id", now, now, null);
    CommentData commentData2 = new CommentData("id", "body", "article-id", now, now, null);

    assertEquals(commentData1, commentData2);
    assertEquals(commentData1.hashCode(), commentData2.hashCode());
  }

  @Test
  void toString_notNull() {
    CommentData commentData = new CommentData();
    assertNotNull(commentData.toString());
  }
}
