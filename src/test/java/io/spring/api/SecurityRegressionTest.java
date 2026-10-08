package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.TestHelper;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.TagsQueryService;
import io.spring.application.UserQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.user.UserService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticleApi.class, ArticlesApi.class, CurrentUserApi.class, TagsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class SecurityRegressionTest extends TestWithCurrentUser {
  private static final String EXPIRED_LOOKING_TOKEN =
      "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMjMiLCJleHAiOjF9.c2lnbmF0dXJl";

  @Autowired private MockMvc mvc;

  @MockitoBean private ArticleQueryService articleQueryService;
  @MockitoBean private ArticleRepository articleRepository;
  @MockitoBean private ArticleCommandService articleCommandService;
  @MockitoBean private TagsQueryService tagsQueryService;
  @MockitoBean private UserQueryService userQueryService;
  @MockitoBean private UserService userService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_return_401_for_protected_endpoints_without_token() throws Exception {
    given().when().get("/user").then().statusCode(401);
    given().when().get("/articles/feed").then().statusCode(401);
    given()
        .contentType("application/json")
        .body(articleParam())
        .when()
        .post("/articles")
        .then()
        .statusCode(401);
    given().when().delete("/articles/{slug}", "some-article").then().statusCode(401);
  }

  @Test
  public void should_treat_invalid_token_as_anonymous_on_protected_endpoint() throws Exception {
    given().header("Authorization", "Token not-a-jwt").when().get("/user").then().statusCode(401);
  }

  @Test
  public void should_treat_expired_looking_token_as_anonymous() throws Exception {
    when(jwtService.getSubFromToken(eq(EXPIRED_LOOKING_TOKEN))).thenReturn(Optional.empty());

    given()
        .header("Authorization", "Token " + EXPIRED_LOOKING_TOKEN)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(401);
  }

  @Test
  public void should_treat_malformed_authorization_header_as_anonymous() throws Exception {
    given().header("Authorization", "Token").when().get("/user").then().statusCode(401);
    given().header("Authorization", token).when().get("/user").then().statusCode(401);
    given().header("Authorization", "").when().get("/user").then().statusCode(401);
  }

  @Test
  public void should_return_401_when_token_subject_is_unknown_user() throws Exception {
    when(jwtService.getSubFromToken(eq("orphan"))).thenReturn(Optional.of("missing-user-id"));
    when(userRepository.findById(eq("missing-user-id"))).thenReturn(Optional.empty());

    given().header("Authorization", "Token orphan").when().get("/user").then().statusCode(401);
  }

  @Test
  public void should_return_200_on_public_endpoint_with_invalid_token() throws Exception {
    Article article = new Article("Public Article", "desc", "body", Arrays.asList("tag"), "author");
    ArticleData articleData = TestHelper.getArticleDataFromArticleAndUser(article, user);
    when(articleQueryService.findBySlug(eq(article.getSlug()), isNull()))
        .thenReturn(Optional.of(articleData));

    given()
        .header("Authorization", "Token garbage")
        .when()
        .get("/articles/{slug}", article.getSlug())
        .then()
        .statusCode(200)
        .body("article.slug", equalTo(article.getSlug()));

    verify(articleQueryService).findBySlug(eq(article.getSlug()), isNull());
  }

  @Test
  public void should_allow_public_endpoints_without_token() throws Exception {
    given().when().get("/tags").then().statusCode(200);
    given().when().get("/articles").then().statusCode(200);
    given().when().options("/articles").then().statusCode(200);
  }

  @Test
  public void should_return_current_user_with_valid_token() throws Exception {
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("user.email", equalTo(email))
        .body("user.username", equalTo(username))
        .body("user.token", equalTo(token));
  }

  @Test
  public void should_use_second_element_of_authorization_header_as_token() throws Exception {
    when(userQueryService.findById(eq(user.getId()))).thenReturn(Optional.of(userData));

    given()
        .header("Authorization", "Bearer " + token)
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("user.username", equalTo(username));
  }

  @Test
  public void should_allow_feed_with_valid_token() throws Exception {
    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/articles/feed")
        .then()
        .statusCode(200);

    verify(articleQueryService).findUserFeed(eq(user), any());
  }

  @Test
  public void should_return_403_when_updating_another_users_article() throws Exception {
    User anotherUser = new User("other@test.com", "other", "123123", "", "");
    Article article =
        new Article("Their Title", "desc", "body", Arrays.asList("java"), anotherUser.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(updateParam())
        .when()
        .put("/articles/{slug}", article.getSlug())
        .then()
        .statusCode(403);

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}", article.getSlug())
        .then()
        .statusCode(403);
  }

  @Test
  public void should_return_404_for_missing_article() throws Exception {
    when(articleQueryService.findBySlug(eq("missing"), isNull())).thenReturn(Optional.empty());
    given().when().get("/articles/{slug}", "missing").then().statusCode(404);

    when(articleRepository.findBySlug(eq("missing"))).thenReturn(Optional.empty());
    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}", "missing")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_not_require_authentication_for_error_dispatch() throws Exception {
    // @ResponseStatus exceptions (403/404) are rendered through the container's ERROR dispatch
    // to /error; if that dispatch were treated as a protected request the client would see 401.
    mvc.perform(
            get("/error")
                .with(
                    request -> {
                      request.setDispatcherType(DispatcherType.ERROR);
                      request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 403);
                      request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/articles/x");
                      return request;
                    }))
        .andExpect(result -> assertNotEquals(401, result.getResponse().getStatus()));

    mvc.perform(get("/error"))
        .andExpect(result -> assertEquals(401, result.getResponse().getStatus()));
  }

  private Map<String, Object> articleParam() {
    Map<String, Object> article = new HashMap<>();
    article.put("title", "title");
    article.put("description", "description");
    article.put("body", "body");
    Map<String, Object> param = new HashMap<>();
    param.put("article", article);
    return param;
  }

  private Map<String, Object> updateParam() {
    Map<String, Object> article = new HashMap<>();
    article.put("title", "new title");
    article.put("body", "new body");
    article.put("description", "new description");
    Map<String, Object> param = new HashMap<>();
    param.put("article", article);
    return param;
  }
}
