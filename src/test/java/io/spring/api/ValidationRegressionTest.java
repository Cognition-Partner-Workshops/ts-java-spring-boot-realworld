package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.CommentQueryService;
import io.spring.application.UserQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.user.UserService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.CommentRepository;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ArticlesApi.class, CommentsApi.class, UsersApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ValidationRegressionTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockitoBean private ArticleQueryService articleQueryService;
  @MockitoBean private ArticleCommandService articleCommandService;
  @MockitoBean private ArticleRepository articleRepository;
  @MockitoBean private CommentRepository commentRepository;
  @MockitoBean private CommentQueryService commentQueryService;
  @MockitoBean private UserQueryService userQueryService;
  @MockitoBean private UserService userService;
  @MockitoBean private PasswordEncoder passwordEncoder;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_return_422_with_field_errors_for_blank_article_fields() throws Exception {
    Map<String, Object> article = new HashMap<>();
    article.put("title", "");
    article.put("description", " ");
    article.put("body", null);
    Map<String, Object> param = new HashMap<>();
    param.put("article", article);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles")
        .then()
        .statusCode(422)
        .contentType(containsString("application/json"))
        .body("errors.title[0]", equalTo("can't be empty"))
        .body("errors.description[0]", equalTo("can't be empty"))
        .body("errors.body[0]", equalTo("can't be empty"));
  }

  @Test
  public void should_return_422_for_blank_comment_body() throws Exception {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    Map<String, Object> comment = new HashMap<>();
    comment.put("body", "");
    Map<String, Object> param = new HashMap<>();
    param.put("comment", comment);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(422)
        .body("errors.body[0]", equalTo("can't be empty"));
  }

  @Test
  public void should_return_422_for_invalid_registration() throws Exception {
    when(userRepository.findByUsername(eq(""))).thenReturn(Optional.empty());
    when(userRepository.findByEmail(eq("not-an-email"))).thenReturn(Optional.empty());

    Map<String, Object> userParam = new HashMap<>();
    userParam.put("email", "not-an-email");
    userParam.put("username", "");
    userParam.put("password", "");
    Map<String, Object> param = new HashMap<>();
    param.put("user", userParam);

    given()
        .contentType("application/json")
        .body(param)
        .when()
        .post("/users")
        .then()
        .statusCode(422)
        .body("errors.email[0]", equalTo("should be an email"))
        .body("errors.username[0]", equalTo("can't be empty"))
        .body("errors.password[0]", equalTo("can't be empty"));
  }

  @Test
  public void should_return_400_with_error_body_for_malformed_json() throws Exception {
    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body("{\"article\": {\"title\": \"unterminated")
        .when()
        .post("/articles")
        .then()
        .statusCode(400)
        .body("status", equalTo(400))
        .body("title", equalTo("Bad Request"));
  }

  @Test
  public void should_return_400_with_error_body_for_malformed_json_on_public_endpoint()
      throws Exception {
    given()
        .contentType("application/json")
        .body("not json at all")
        .when()
        .post("/users/login")
        .then()
        .statusCode(400)
        .body("status", equalTo(400))
        .body("$", not(hasKey("trace")));
  }

  @Test
  public void should_return_400_when_root_wrapper_is_missing() throws Exception {
    Map<String, Object> param = new HashMap<>();
    param.put("title", "no wrapper");

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles")
        .then()
        .statusCode(400)
        .body("status", equalTo(400));
  }

  @Test
  public void should_return_415_for_unsupported_media_type() throws Exception {
    given()
        .contentType("text/plain")
        .header("Authorization", "Token " + token)
        .body("title=x")
        .when()
        .post("/articles")
        .then()
        .statusCode(415);
  }
}
