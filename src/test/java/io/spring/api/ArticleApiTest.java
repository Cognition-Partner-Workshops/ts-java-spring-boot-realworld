package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.TestHelper;
import io.spring.api.adapter.RestToGraphQLAdapter;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticleApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private RestToGraphQLAdapter restToGraphQLAdapter;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_read_article_success() throws Exception {
    String slug = "test-new-article";
    DateTime time = new DateTime();
    Article article =
        new Article(
            "Test New Article",
            "Desc",
            "Body",
            Arrays.asList("java", "spring", "jpg"),
            user.getId(),
            time);
    ArticleData articleData = TestHelper.getArticleDataFromArticleAndUser(article, user);

    Map<String, Object> articleResponse =
        new HashMap<String, Object>() {
          {
            put("article", articleData);
          }
        };

    when(restToGraphQLAdapter.getArticle(eq(slug), eq(null))).thenReturn(articleResponse);

    RestAssuredMockMvc.when()
        .get("/articles/{slug}", slug)
        .then()
        .statusCode(200)
        .body("article.slug", equalTo(slug))
        .body("article.body", equalTo(articleData.getBody()))
        .body("article.createdAt", equalTo(ISODateTimeFormat.dateTime().withZoneUTC().print(time)));
  }

  @Test
  public void should_404_if_article_not_found() throws Exception {
    when(restToGraphQLAdapter.getArticle(any(), any()))
        .thenThrow(new ResourceNotFoundException());
    RestAssuredMockMvc.when().get("/articles/not-exists").then().statusCode(404);
  }

  @Test
  public void should_update_article_content_success() throws Exception {
    Article updatedArticle =
        new Article(
            "new title",
            "new description",
            "new body",
            Arrays.asList("java", "spring", "jpg"),
            user.getId());

    Map<String, Object> updateParam =
        prepareUpdateParam(
            updatedArticle.getTitle(), updatedArticle.getBody(), updatedArticle.getDescription());

    ArticleData updatedArticleData =
        TestHelper.getArticleDataFromArticleAndUser(updatedArticle, user);

    Map<String, Object> articleResponse =
        new HashMap<String, Object>() {
          {
            put("article", updatedArticleData);
          }
        };

    when(restToGraphQLAdapter.updateArticle(any(), any(), any(), any(), any()))
        .thenReturn(articleResponse);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(updateParam)
        .when()
        .put("/articles/{slug}", "old-title")
        .then()
        .statusCode(200)
        .body("article.slug", equalTo(updatedArticleData.getSlug()));
  }

  @Test
  public void should_get_403_if_not_author_to_update_article() throws Exception {
    String title = "new-title";
    String body = "new body";
    String description = "new description";
    Map<String, Object> updateParam = prepareUpdateParam(title, body, description);

    when(restToGraphQLAdapter.updateArticle(any(), any(), any(), any(), any()))
        .thenThrow(new NoAuthorizationException());

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(updateParam)
        .when()
        .put("/articles/{slug}", "some-article")
        .then()
        .statusCode(403);
  }

  @Test
  public void should_delete_article_success() throws Exception {
    doNothing().when(restToGraphQLAdapter).deleteArticle(any());

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}", "some-article")
        .then()
        .statusCode(204);
  }

  @Test
  public void should_403_if_not_author_delete_article() throws Exception {
    doThrow(new NoAuthorizationException()).when(restToGraphQLAdapter).deleteArticle(any());

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}", "some-article")
        .then()
        .statusCode(403);
  }

  private HashMap<String, Object> prepareUpdateParam(
      final String title, final String body, final String description) {
    return new HashMap<String, Object>() {
      {
        put(
            "article",
            new HashMap<String, Object>() {
              {
                put("title", title);
                put("body", body);
                put("description", description);
              }
            });
      }
    };
  }
}
