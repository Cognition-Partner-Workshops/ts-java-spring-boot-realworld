package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  @Test
  void constructor_and_getters() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image", false);
    ArticleData articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "description",
            "body",
            true,
            5,
            now,
            now,
            Arrays.asList("java", "spring"),
            profileData);

    assertEquals("article-id", articleData.getId());
    assertEquals("test-slug", articleData.getSlug());
    assertEquals("Test Title", articleData.getTitle());
    assertEquals("description", articleData.getDescription());
    assertEquals("body", articleData.getBody());
    assertTrue(articleData.isFavorited());
    assertEquals(5, articleData.getFavoritesCount());
    assertEquals(now, articleData.getCreatedAt());
    assertEquals(now, articleData.getUpdatedAt());
    assertEquals(2, articleData.getTagList().size());
    assertEquals(profileData, articleData.getProfileData());
  }

  @Test
  void getCursor_returnsDateTimeCursor() {
    DateTime updatedAt = DateTime.now();
    ArticleData articleData =
        new ArticleData(
            "id", "slug", "title", "desc", "body", false, 0, DateTime.now(), updatedAt, null, null);

    DateTimeCursor cursor = articleData.getCursor();

    assertNotNull(cursor);
    assertEquals(updatedAt, cursor.getData());
  }

  @Test
  void setters() {
    ArticleData articleData = new ArticleData();
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image", false);

    articleData.setId("new-id");
    articleData.setSlug("new-slug");
    articleData.setTitle("New Title");
    articleData.setDescription("new description");
    articleData.setBody("new body");
    articleData.setFavorited(true);
    articleData.setFavoritesCount(10);
    articleData.setCreatedAt(now);
    articleData.setUpdatedAt(now);
    articleData.setTagList(Arrays.asList("kotlin"));
    articleData.setProfileData(profileData);

    assertEquals("new-id", articleData.getId());
    assertEquals("new-slug", articleData.getSlug());
    assertEquals("New Title", articleData.getTitle());
    assertEquals("new description", articleData.getDescription());
    assertEquals("new body", articleData.getBody());
    assertTrue(articleData.isFavorited());
    assertEquals(10, articleData.getFavoritesCount());
    assertEquals(now, articleData.getCreatedAt());
    assertEquals(now, articleData.getUpdatedAt());
    assertEquals(1, articleData.getTagList().size());
    assertEquals(profileData, articleData.getProfileData());
  }

  @Test
  void equals_and_hashCode() {
    DateTime now = DateTime.now();
    ArticleData articleData1 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);
    ArticleData articleData2 =
        new ArticleData("id", "slug", "title", "desc", "body", false, 0, now, now, null, null);

    assertEquals(articleData1, articleData2);
    assertEquals(articleData1.hashCode(), articleData2.hashCode());
  }

  @Test
  void toString_notNull() {
    ArticleData articleData = new ArticleData();
    assertNotNull(articleData.toString());
  }
}
