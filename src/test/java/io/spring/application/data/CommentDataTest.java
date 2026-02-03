package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class CommentDataTest {

  @Test
  void constructor_createsWithAllFields() {
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image.jpg", false);
    DateTime now = DateTime.now();

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
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image.jpg", false);
    DateTime createdAt = DateTime.now();

    CommentData commentData =
        new CommentData("comment-id", "Test body", "article-id", createdAt, createdAt, profileData);

    DateTimeCursor cursor = commentData.getCursor();

    assertNotNull(cursor);
    assertEquals(createdAt, cursor.getData());
  }

  @Test
  void noArgsConstructor_createsEmptyObject() {
    CommentData commentData = new CommentData();

    assertNull(commentData.getId());
    assertNull(commentData.getBody());
    assertNull(commentData.getArticleId());
  }

  @Test
  void setters_updateFields() {
    CommentData commentData = new CommentData();
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image.jpg", false);
    DateTime now = DateTime.now();

    commentData.setId("new-id");
    commentData.setBody("New body");
    commentData.setArticleId("new-article-id");
    commentData.setCreatedAt(now);
    commentData.setUpdatedAt(now);
    commentData.setProfileData(profileData);

    assertEquals("new-id", commentData.getId());
    assertEquals("New body", commentData.getBody());
    assertEquals("new-article-id", commentData.getArticleId());
    assertEquals(now, commentData.getCreatedAt());
    assertEquals(now, commentData.getUpdatedAt());
    assertEquals(profileData, commentData.getProfileData());
  }

  @Test
  void equals_withSameData() {
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image.jpg", false);
    DateTime now = DateTime.now();

    CommentData commentData1 =
        new CommentData("comment-id", "Test body", "article-id", now, now, profileData);

    CommentData commentData2 =
        new CommentData("comment-id", "Test body", "article-id", now, now, profileData);

    assertEquals(commentData1, commentData2);
    assertEquals(commentData1.hashCode(), commentData2.hashCode());
  }
}
