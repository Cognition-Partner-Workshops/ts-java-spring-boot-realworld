package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  @Test
  public void should_create_article_data_with_all_args_constructor() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("id1", "author", "bio", "image", false);
    ArticleData articleData = new ArticleData(
        "id123", "test-slug", "Test Title", "Description", "Body content",
        true, 5, now, now, Arrays.asList("java", "spring"), profileData);
    
    assertThat(articleData.getId(), is("id123"));
    assertThat(articleData.getSlug(), is("test-slug"));
    assertThat(articleData.getTitle(), is("Test Title"));
    assertThat(articleData.getDescription(), is("Description"));
    assertThat(articleData.getBody(), is("Body content"));
    assertThat(articleData.isFavorited(), is(true));
    assertThat(articleData.getFavoritesCount(), is(5));
    assertThat(articleData.getCreatedAt(), is(now));
    assertThat(articleData.getUpdatedAt(), is(now));
    assertThat(articleData.getTagList().size(), is(2));
    assertThat(articleData.getProfileData(), is(profileData));
  }

  @Test
  public void should_create_article_data_with_no_args_constructor() {
    ArticleData articleData = new ArticleData();
    
    assertThat(articleData.getId(), nullValue());
    assertThat(articleData.getSlug(), nullValue());
    assertThat(articleData.getTitle(), nullValue());
    assertThat(articleData.isFavorited(), is(false));
    assertThat(articleData.getFavoritesCount(), is(0));
  }

  @Test
  public void should_set_fields_via_setters() {
    ArticleData articleData = new ArticleData();
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("id1", "author", "bio", "image", false);
    
    articleData.setId("id123");
    articleData.setSlug("test-slug");
    articleData.setTitle("Test Title");
    articleData.setDescription("Description");
    articleData.setBody("Body content");
    articleData.setFavorited(true);
    articleData.setFavoritesCount(5);
    articleData.setCreatedAt(now);
    articleData.setUpdatedAt(now);
    articleData.setTagList(Arrays.asList("java", "spring"));
    articleData.setProfileData(profileData);
    
    assertThat(articleData.getId(), is("id123"));
    assertThat(articleData.getSlug(), is("test-slug"));
    assertThat(articleData.getTitle(), is("Test Title"));
    assertThat(articleData.getDescription(), is("Description"));
    assertThat(articleData.getBody(), is("Body content"));
    assertThat(articleData.isFavorited(), is(true));
    assertThat(articleData.getFavoritesCount(), is(5));
    assertThat(articleData.getCreatedAt(), is(now));
    assertThat(articleData.getUpdatedAt(), is(now));
    assertThat(articleData.getTagList().size(), is(2));
    assertThat(articleData.getProfileData(), is(profileData));
  }

  @Test
  public void should_return_cursor_based_on_updated_at() {
    DateTime now = DateTime.now();
    ArticleData articleData = new ArticleData();
    articleData.setUpdatedAt(now);
    
    DateTimeCursor cursor = articleData.getCursor();
    
    assertThat(cursor, notNullValue());
  }

  @Test
  public void should_have_equals_based_on_all_fields() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("id1", "author", "bio", "image", false);
    ArticleData articleData1 = new ArticleData(
        "id123", "test-slug", "Test Title", "Description", "Body content",
        true, 5, now, now, Arrays.asList("java", "spring"), profileData);
    ArticleData articleData2 = new ArticleData(
        "id123", "test-slug", "Test Title", "Description", "Body content",
        true, 5, now, now, Arrays.asList("java", "spring"), profileData);
    ArticleData articleData3 = new ArticleData(
        "id456", "test-slug", "Test Title", "Description", "Body content",
        true, 5, now, now, Arrays.asList("java", "spring"), profileData);
    
    assertThat(articleData1.equals(articleData2), is(true));
    assertThat(articleData1.equals(articleData3), is(false));
  }

  @Test
  public void should_have_hashcode_based_on_all_fields() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("id1", "author", "bio", "image", false);
    ArticleData articleData1 = new ArticleData(
        "id123", "test-slug", "Test Title", "Description", "Body content",
        true, 5, now, now, Arrays.asList("java", "spring"), profileData);
    ArticleData articleData2 = new ArticleData(
        "id123", "test-slug", "Test Title", "Description", "Body content",
        true, 5, now, now, Arrays.asList("java", "spring"), profileData);
    
    assertThat(articleData1.hashCode(), is(articleData2.hashCode()));
  }

  @Test
  public void should_have_toString() {
    ArticleData articleData = new ArticleData();
    articleData.setId("id123");
    articleData.setSlug("test-slug");
    String toString = articleData.toString();
    
    assertThat(toString.contains("id123"), is(true));
    assertThat(toString.contains("test-slug"), is(true));
  }

  @Test
  public void should_handle_empty_tag_list() {
    ArticleData articleData = new ArticleData();
    articleData.setTagList(Collections.emptyList());
    
    assertThat(articleData.getTagList().isEmpty(), is(true));
  }

  @Test
  public void should_not_equal_null() {
    ArticleData articleData = new ArticleData();
    assertThat(articleData.equals(null), is(false));
  }

  @Test
  public void should_not_equal_different_type() {
    ArticleData articleData = new ArticleData();
    assertThat(articleData.equals("string"), is(false));
  }
}
