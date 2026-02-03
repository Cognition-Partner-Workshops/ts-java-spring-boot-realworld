package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class ArticleDataTest {

  @Test
  void constructor_createsWithAllFields() {
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image.jpg", false);
    DateTime now = DateTime.now();

    ArticleData articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            true,
            5,
            now,
            now,
            Arrays.asList("tag1", "tag2"),
            profileData);

    assertEquals("article-id", articleData.getId());
    assertEquals("test-slug", articleData.getSlug());
    assertEquals("Test Title", articleData.getTitle());
    assertEquals("Test Description", articleData.getDescription());
    assertEquals("Test Body", articleData.getBody());
    assertTrue(articleData.isFavorited());
    assertEquals(5, articleData.getFavoritesCount());
    assertEquals(now, articleData.getCreatedAt());
    assertEquals(now, articleData.getUpdatedAt());
    assertEquals(2, articleData.getTagList().size());
    assertEquals(profileData, articleData.getProfileData());
  }

  @Test
  void getCursor_returnsDateTimeCursor() {
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image.jpg", false);
    DateTime updatedAt = DateTime.now();

    ArticleData articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            0,
            DateTime.now(),
            updatedAt,
            Collections.emptyList(),
            profileData);

    DateTimeCursor cursor = articleData.getCursor();

    assertNotNull(cursor);
    assertEquals(updatedAt, cursor.getData());
  }

  @Test
  void setters_updateFields() {
    ArticleData articleData = new ArticleData();

    articleData.setId("new-id");
    articleData.setSlug("new-slug");
    articleData.setTitle("New Title");
    articleData.setDescription("New Description");
    articleData.setBody("New Body");
    articleData.setFavorited(true);
    articleData.setFavoritesCount(10);

    assertEquals("new-id", articleData.getId());
    assertEquals("new-slug", articleData.getSlug());
    assertEquals("New Title", articleData.getTitle());
    assertEquals("New Description", articleData.getDescription());
    assertEquals("New Body", articleData.getBody());
    assertTrue(articleData.isFavorited());
    assertEquals(10, articleData.getFavoritesCount());
  }

  @Test
  void noArgsConstructor_createsEmptyObject() {
    ArticleData articleData = new ArticleData();

    assertNull(articleData.getId());
    assertNull(articleData.getSlug());
    assertNull(articleData.getTitle());
    assertFalse(articleData.isFavorited());
    assertEquals(0, articleData.getFavoritesCount());
  }

  @Test
  void equals_withSameData() {
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image.jpg", false);
    DateTime now = DateTime.now();

    ArticleData articleData1 =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            profileData);

    ArticleData articleData2 =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            0,
            now,
            now,
            Collections.emptyList(),
            profileData);

    assertEquals(articleData1, articleData2);
    assertEquals(articleData1.hashCode(), articleData2.hashCode());
  }
}
