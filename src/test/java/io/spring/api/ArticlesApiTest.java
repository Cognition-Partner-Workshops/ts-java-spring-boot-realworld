package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticlesApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticlesApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleQueryService articleQueryService;

  @MockBean private ArticleCommandService articleCommandService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_create_article_success() throws Exception {
    String title = "How to train your dragon";
    String slug = "how-to-train-your-dragon";
    String description = "Ever wonder how?";
    String body = "You have to believe";
    List<String> tagList = asList("reactjs", "angularjs", "dragons");
    Map<String, Object> param = prepareParam(title, description, body, tagList);

    ArticleData articleData =
        new ArticleData(
            "123",
            slug,
            title,
            description,
            body,
            false,
            0,
            new DateTime(),
            new DateTime(),
            tagList,
            new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false));

    when(articleCommandService.createArticle(any(), any()))
        .thenReturn(new Article(title, description, body, tagList, user.getId()));

    when(articleQueryService.findBySlug(eq(Article.toSlug(title)), any()))
        .thenReturn(Optional.empty());

    when(articleQueryService.findById(any(), any())).thenReturn(Optional.of(articleData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles")
        .then()
        .statusCode(200)
        .body("article.title", equalTo(title))
        .body("article.favorited", equalTo(false))
        .body("article.body", equalTo(body))
        .body("article.favoritesCount", equalTo(0))
        .body("article.author.username", equalTo(user.getUsername()))
        .body("article.author.id", equalTo(null));

    verify(articleCommandService).createArticle(any(), any());
  }

  @Test
  public void should_get_error_message_with_wrong_parameter() throws Exception {
    String title = "How to train your dragon";
    String description = "Ever wonder how?";
    String body = "";
    String[] tagList = {"reactjs", "angularjs", "dragons"};
    Map<String, Object> param = prepareParam(title, description, body, asList(tagList));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles")
        .prettyPeek()
        .then()
        .statusCode(422)
        .body("errors.body[0]", equalTo("can't be empty"));
  }

  @Test
  public void should_get_error_message_with_duplicated_title() {
    String title = "How to train your dragon";
    String slug = "how-to-train-your-dragon";
    String description = "Ever wonder how?";
    String body = "You have to believe";
    String[] tagList = {"reactjs", "angularjs", "dragons"};
    Map<String, Object> param = prepareParam(title, description, body, asList(tagList));

    ArticleData articleData =
        new ArticleData(
            "123",
            slug,
            title,
            description,
            body,
            false,
            0,
            new DateTime(),
            new DateTime(),
            asList(tagList),
            new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false));

    when(articleQueryService.findBySlug(eq(Article.toSlug(title)), any()))
        .thenReturn(Optional.of(articleData));

    when(articleQueryService.findById(any(), any())).thenReturn(Optional.of(articleData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles")
        .prettyPeek()
        .then()
        .statusCode(422);
  }

  @Test
  public void should_get_422_with_missing_title() throws Exception {
    String description = "Ever wonder how?";
    String body = "You have to believe";
    List<String> tagList = asList("reactjs", "angularjs");
    Map<String, Object> param = prepareParam("", description, body, tagList);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles")
        .then()
        .statusCode(422)
        .body("errors.title[0]", equalTo("can't be empty"));
  }

  @Test
  public void should_get_422_with_missing_description() throws Exception {
    String title = "How to train your dragon";
    String body = "You have to believe";
    List<String> tagList = asList("reactjs", "angularjs");
    Map<String, Object> param = prepareParam(title, "", body, tagList);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles")
        .then()
        .statusCode(422)
        .body("errors.description[0]", equalTo("can't be empty"));
  }

  @Test
  public void should_get_401_without_authentication() throws Exception {
    String title = "How to train your dragon";
    String description = "Ever wonder how?";
    String body = "You have to believe";
    List<String> tagList = asList("reactjs", "angularjs");
    Map<String, Object> param = prepareParam(title, description, body, tagList);

    given()
        .contentType("application/json")
        .body(param)
        .when()
        .post("/articles")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_get_articles_with_tag_filter() throws Exception {
    ArticleData articleData =
        new ArticleData(
            "123",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            asList("java", "spring"),
            new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false));

    when(articleQueryService.findRecentArticles(
            eq("java"), eq(null), eq(null), any(Page.class), eq(null)))
        .thenReturn(new io.spring.application.data.ArticleDataList(asList(articleData), 1));

    given()
        .contentType("application/json")
        .when()
        .get("/articles?tag=java")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_articles_with_author_filter() throws Exception {
    ArticleData articleData =
        new ArticleData(
            "123",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            asList("java"),
            new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false));

    when(articleQueryService.findRecentArticles(
            eq(null), eq("testauthor"), eq(null), any(Page.class), eq(null)))
        .thenReturn(new io.spring.application.data.ArticleDataList(asList(articleData), 1));

    given()
        .contentType("application/json")
        .when()
        .get("/articles?author=testauthor")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_articles_with_favorited_filter() throws Exception {
    ArticleData articleData =
        new ArticleData(
            "123",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            true,
            5,
            new DateTime(),
            new DateTime(),
            asList("java"),
            new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false));

    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq("favoriteuser"), any(Page.class), eq(null)))
        .thenReturn(new io.spring.application.data.ArticleDataList(asList(articleData), 1));

    given()
        .contentType("application/json")
        .when()
        .get("/articles?favorited=favoriteuser")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_articles_with_pagination() throws Exception {
    ArticleData articleData =
        new ArticleData(
            "123",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            asList("java"),
            new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false));

    when(articleQueryService.findRecentArticles(
            eq(null), eq(null), eq(null), eq(new Page(10, 5)), eq(null)))
        .thenReturn(new io.spring.application.data.ArticleDataList(asList(articleData), 1));

    given()
        .contentType("application/json")
        .when()
        .get("/articles?offset=10&limit=5")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_user_feed_with_authentication() throws Exception {
    ArticleData articleData =
        new ArticleData(
            "123",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            asList("java"),
            new ProfileData("userid", user.getUsername(), user.getBio(), user.getImage(), false));

    when(articleQueryService.findUserFeed(eq(user), any(Page.class)))
        .thenReturn(new io.spring.application.data.ArticleDataList(asList(articleData), 1));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200);
  }

  @Test
  public void should_get_401_for_feed_without_authentication() throws Exception {
    given()
        .contentType("application/json")
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(401);
  }

  private HashMap<String, Object> prepareParam(
      final String title, final String description, final String body, final List<String> tagList) {
    return new HashMap<String, Object>() {
      {
        put(
            "article",
            new HashMap<String, Object>() {
              {
                put("title", title);
                put("description", description);
                put("body", body);
                put("tagList", tagList);
              }
            });
      }
    };
  }
}
