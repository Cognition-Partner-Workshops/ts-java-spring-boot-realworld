package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataListTest {

  @Test
  public void should_create_article_data_list_with_articles_and_count() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("id1", "author", "bio", "image", false);
    ArticleData articleData = new ArticleData(
        "id123", "test-slug", "Test Title", "Description", "Body content",
        true, 5, now, now, Arrays.asList("java", "spring"), profileData);
    
    ArticleDataList articleDataList = new ArticleDataList(Arrays.asList(articleData), 1);
    
    assertThat(articleDataList.getArticleDatas().size(), is(1));
    assertThat(articleDataList.getCount(), is(1));
  }

  @Test
  public void should_create_empty_article_data_list() {
    ArticleDataList articleDataList = new ArticleDataList(Collections.emptyList(), 0);
    
    assertThat(articleDataList.getArticleDatas().isEmpty(), is(true));
    assertThat(articleDataList.getCount(), is(0));
  }

  @Test
  public void should_handle_count_different_from_list_size() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("id1", "author", "bio", "image", false);
    ArticleData articleData = new ArticleData(
        "id123", "test-slug", "Test Title", "Description", "Body content",
        true, 5, now, now, Arrays.asList("java", "spring"), profileData);
    
    ArticleDataList articleDataList = new ArticleDataList(Arrays.asList(articleData), 100);
    
    assertThat(articleDataList.getArticleDatas().size(), is(1));
    assertThat(articleDataList.getCount(), is(100));
  }

  @Test
  public void should_handle_multiple_articles() {
    DateTime now = DateTime.now();
    ProfileData profileData = new ProfileData("id1", "author", "bio", "image", false);
    ArticleData articleData1 = new ArticleData(
        "id1", "slug-1", "Title 1", "Desc 1", "Body 1",
        true, 5, now, now, Arrays.asList("java"), profileData);
    ArticleData articleData2 = new ArticleData(
        "id2", "slug-2", "Title 2", "Desc 2", "Body 2",
        false, 0, now, now, Arrays.asList("spring"), profileData);
    
    ArticleDataList articleDataList = new ArticleDataList(Arrays.asList(articleData1, articleData2), 2);
    
    assertThat(articleDataList.getArticleDatas().size(), is(2));
    assertThat(articleDataList.getCount(), is(2));
  }
}
