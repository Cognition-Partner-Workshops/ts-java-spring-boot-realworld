package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.TestHelper;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticleByIdApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleByIdApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private ArticleQueryService articleQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_read_article_by_id_success() throws Exception {
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

    when(articleQueryService.findById(eq(article.getId()), eq(null)))
        .thenReturn(Optional.of(articleData));

    RestAssuredMockMvc.when()
        .get("/articles/id/{id}", article.getId())
        .then()
        .statusCode(200)
        .body("article.id", equalTo(article.getId()))
        .body("article.body", equalTo(articleData.getBody()))
        .body("article.createdAt", equalTo(ISODateTimeFormat.dateTime().withZoneUTC().print(time)));
  }

  @Test
  public void should_read_article_by_id_with_auth_success() throws Exception {
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

    when(articleQueryService.findById(eq(article.getId()), eq(user)))
        .thenReturn(Optional.of(articleData));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/id/{id}", article.getId())
        .then()
        .statusCode(200)
        .body("article.id", equalTo(article.getId()))
        .body("article.body", equalTo(articleData.getBody()));
  }

  @Test
  public void should_404_if_article_not_found_by_id() throws Exception {
    when(articleQueryService.findById(anyString(), any())).thenReturn(Optional.empty());
    RestAssuredMockMvc.when()
        .get("/articles/id/{id}", "non-existent-id")
        .then()
        .statusCode(404);
  }
}
