package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.application.DateTimeCursor;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataTest {

  @Test
  public void should_create_article_data_with_all_args_constructor() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    List<String> tags = Arrays.asList("java", "spring");
    
    ArticleData articleData = new ArticleData(
        "id", "slug", "title", "description", "body",
        true, 5, now, now, tags, profileData);

    assertThat(articleData.getId(), is("id"));
    assertThat(articleData.getSlug(), is("slug"));
    assertThat(articleData.getTitle(), is("title"));
    assertThat(articleData.getDescription(), is("description"));
    assertThat(articleData.getBody(), is("body"));
    assertThat(articleData.isFavorited(), is(true));
    assertThat(articleData.getFavoritesCount(), is(5));
    assertThat(articleData.getCreatedAt(), is(now));
    assertThat(articleData.getUpdatedAt(), is(now));
    assertThat(articleData.getTagList(), is(tags));
    assertThat(articleData.getProfileData(), is(profileData));
  }

  @Test
  public void should_create_article_data_with_no_args_constructor() {
    ArticleData articleData = new ArticleData();
    
    assertThat(articleData.getId(), is((String) null));
    assertThat(articleData.getSlug(), is((String) null));
  }

  @Test
  public void should_set_and_get_properties() {
    ArticleData articleData = new ArticleData();
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    List<String> tags = Arrays.asList("java", "spring");
    
    articleData.setId("id");
    articleData.setSlug("slug");
    articleData.setTitle("title");
    articleData.setDescription("description");
    articleData.setBody("body");
    articleData.setFavorited(true);
    articleData.setFavoritesCount(5);
    articleData.setCreatedAt(now);
    articleData.setUpdatedAt(now);
    articleData.setTagList(tags);
    articleData.setProfileData(profileData);

    assertThat(articleData.getId(), is("id"));
    assertThat(articleData.getSlug(), is("slug"));
    assertThat(articleData.getTitle(), is("title"));
    assertThat(articleData.getDescription(), is("description"));
    assertThat(articleData.getBody(), is("body"));
    assertThat(articleData.isFavorited(), is(true));
    assertThat(articleData.getFavoritesCount(), is(5));
    assertThat(articleData.getCreatedAt(), is(now));
    assertThat(articleData.getUpdatedAt(), is(now));
    assertThat(articleData.getTagList(), is(tags));
    assertThat(articleData.getProfileData(), is(profileData));
  }

  @Test
  public void should_get_cursor() {
    DateTime now = DateTime.now();
    ArticleData articleData = new ArticleData();
    articleData.setUpdatedAt(now);

    DateTimeCursor cursor = articleData.getCursor();

    assertThat(cursor, is(notNullValue()));
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "image", false);
    List<String> tags = Arrays.asList("java", "spring");
    
    ArticleData articleData1 = new ArticleData(
        "id", "slug", "title", "description", "body",
        true, 5, now, now, tags, profileData);
    ArticleData articleData2 = new ArticleData(
        "id", "slug", "title", "description", "body",
        true, 5, now, now, tags, profileData);
    ArticleData articleData3 = new ArticleData(
        "different-id", "slug", "title", "description", "body",
        true, 5, now, now, tags, profileData);

    assertThat(articleData1.equals(articleData2), is(true));
    assertThat(articleData1.equals(articleData3), is(false));
    assertThat(articleData1.hashCode(), is(articleData2.hashCode()));
    assertThat(articleData1.hashCode(), is(not(articleData3.hashCode())));
  }

  @Test
  public void should_implement_to_string() {
    ArticleData articleData = new ArticleData();
    articleData.setId("id");
    articleData.setSlug("slug");

    String toString = articleData.toString();

    assertThat(toString, is(notNullValue()));
    assertThat(toString.contains("id"), is(true));
    assertThat(toString.contains("slug"), is(true));
  }

  @Test
  public void should_handle_equals_with_null_and_different_type() {
    ArticleData articleData = new ArticleData();
    articleData.setId("id");

    assertThat(articleData.equals(null), is(false));
    assertThat(articleData.equals("string"), is(false));
    assertThat(articleData.equals(articleData), is(true));
  }

  @Test
  public void should_handle_equals_with_null_fields() {
    ArticleData articleData1 = new ArticleData();
    ArticleData articleData2 = new ArticleData();

    assertThat(articleData1.equals(articleData2), is(true));
    assertThat(articleData1.hashCode(), is(articleData2.hashCode()));
  }
}
