package io.spring.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

public class ArticleDataListTest {

  @Test
  void constructor_and_getters() {
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image", false);
    ArticleData articleData =
        new ArticleData(
            "id", "slug", "title", "desc", "body", false, 0, DateTime.now(), DateTime.now(), null, profileData);
    List<ArticleData> articles = Arrays.asList(articleData);

    ArticleDataList articleDataList = new ArticleDataList(articles, 1);

    assertEquals(1, articleDataList.getCount());
    assertEquals(1, articleDataList.getArticleDatas().size());
    assertEquals(articleData, articleDataList.getArticleDatas().get(0));
  }

  @Test
  void emptyList() {
    ArticleDataList articleDataList = new ArticleDataList(Collections.emptyList(), 0);

    assertEquals(0, articleDataList.getCount());
    assertTrue(articleDataList.getArticleDatas().isEmpty());
  }

  @Test
  void multipleArticles() {
    ProfileData profileData = new ProfileData("user-id", "testuser", "bio", "image", false);
    ArticleData articleData1 =
        new ArticleData(
            "id1", "slug1", "title1", "desc", "body", false, 0, DateTime.now(), DateTime.now(), null, profileData);
    ArticleData articleData2 =
        new ArticleData(
            "id2", "slug2", "title2", "desc", "body", false, 0, DateTime.now(), DateTime.now(), null, profileData);
    List<ArticleData> articles = Arrays.asList(articleData1, articleData2);

    ArticleDataList articleDataList = new ArticleDataList(articles, 10);

    assertEquals(10, articleDataList.getCount());
    assertEquals(2, articleDataList.getArticleDatas().size());
  }
}
