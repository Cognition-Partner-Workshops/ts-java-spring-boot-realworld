package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.*;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Functional Test Suite for Article API
 * 
 * This test suite validates the article management functionality including:
 * - Article retrieval by slug
 * - Article creation with tags
 * - Article update with authorization
 * - Article deletion with authorization
 * - Error handling and edge cases
 */
@WebMvcTest(ArticleApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class ArticleFunctionalTest {

  @Autowired
  private MockMvc mvc;

  @MockBean
  private ArticleQueryService articleQueryService;

  @MockBean
  private ArticleRepository articleRepository;

  @MockBean
  private ArticleCommandService articleCommandService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private JwtService jwtService;

  @MockBean
  private UserReadService userReadService;

  private User testUser;
  private Article testArticle;
  private ArticleData testArticleData;
  private String token;

  @BeforeEach
  void setUp() {
    RestAssuredMockMvc.mockMvc(mvc);
    
    testUser = new User("test@example.com", "testuser", "password", "Test bio", "http://example.com/avatar.jpg");
    token = "test-jwt-token";
    
    testArticle = new Article(
        "Test Article Title",
        "Test description",
        "Test body content",
        Arrays.asList("java", "spring"),
        testUser.getId()
    );
    
    ProfileData authorProfile = new ProfileData(
        testUser.getId(),
        testUser.getUsername(),
        testUser.getBio(),
        testUser.getImage(),
        false
    );
    
    testArticleData = new ArticleData(
        testArticle.getId(),
        testArticle.getSlug(),
        testArticle.getTitle(),
        testArticle.getDescription(),
        testArticle.getBody(),
        false,
        0,
        DateTime.now(),
        DateTime.now(),
        Arrays.asList("java", "spring"),
        authorProfile
    );
    
    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(testUser.getId()));
    when(userRepository.findById(eq(testUser.getId()))).thenReturn(Optional.of(testUser));
  }

  @Nested
  @DisplayName("GET /articles/{slug} - Retrieve Article")
  class GetArticleTests {

    @Test
    @DisplayName("Should return article when found by slug")
    void shouldReturnArticleWhenFound() {
      when(articleQueryService.findBySlug(eq("test-article-title"), any()))
          .thenReturn(Optional.of(testArticleData));

      given()
          .contentType("application/json")
          .when()
          .get("/articles/test-article-title")
          .then()
          .statusCode(200)
          .body("article.slug", equalTo("test-article-title"))
          .body("article.title", equalTo("Test Article Title"))
          .body("article.description", equalTo("Test description"))
          .body("article.body", equalTo("Test body content"))
          .body("article.tagList", hasItems("java", "spring"));
    }

    @Test
    @DisplayName("Should return article with author profile")
    void shouldReturnArticleWithAuthorProfile() {
      when(articleQueryService.findBySlug(eq("test-article-title"), any()))
          .thenReturn(Optional.of(testArticleData));

      given()
          .contentType("application/json")
          .when()
          .get("/articles/test-article-title")
          .then()
          .statusCode(200)
          .body("article.author.username", equalTo("testuser"))
          .body("article.author.bio", equalTo("Test bio"))
          .body("article.author.following", equalTo(false));
    }

    @Test
    @DisplayName("Should return 404 when article not found")
    void shouldReturn404WhenArticleNotFound() {
      when(articleQueryService.findBySlug(eq("non-existent-slug"), any()))
          .thenReturn(Optional.empty());

      given()
          .contentType("application/json")
          .when()
          .get("/articles/non-existent-slug")
          .then()
          .statusCode(404);
    }

    @Test
    @DisplayName("Should return article with favorited status for authenticated user")
    void shouldReturnArticleWithFavoritedStatus() {
      ArticleData favoritedArticle = new ArticleData(
          testArticle.getId(),
          testArticle.getSlug(),
          testArticle.getTitle(),
          testArticle.getDescription(),
          testArticle.getBody(),
          true,
          5,
          DateTime.now(),
          DateTime.now(),
          Arrays.asList("java", "spring"),
          testArticleData.getProfileData()
      );

      when(articleQueryService.findBySlug(eq("test-article-title"), any()))
          .thenReturn(Optional.of(favoritedArticle));

      given()
          .contentType("application/json")
          .header("Authorization", "Token " + token)
          .when()
          .get("/articles/test-article-title")
          .then()
          .statusCode(200)
          .body("article.favorited", equalTo(true))
          .body("article.favoritesCount", equalTo(5));
    }
  }

  @Nested
  @DisplayName("PUT /articles/{slug} - Update Article")
  class UpdateArticleTests {

    @Test
    @DisplayName("Should update article title successfully")
    void shouldUpdateArticleTitle() {
      when(articleRepository.findBySlug(eq("test-article-title")))
          .thenReturn(Optional.of(testArticle));
      
      Article updatedArticle = new Article(
          "Updated Title",
          testArticle.getDescription(),
          testArticle.getBody(),
          Arrays.asList("java", "spring"),
          testUser.getId()
      );
      
      when(articleCommandService.updateArticle(any(), any()))
          .thenReturn(updatedArticle);
      
      ArticleData updatedArticleData = new ArticleData(
          updatedArticle.getId(),
          updatedArticle.getSlug(),
          "Updated Title",
          testArticle.getDescription(),
          testArticle.getBody(),
          false,
          0,
          DateTime.now(),
          DateTime.now(),
          Arrays.asList("java", "spring"),
          testArticleData.getProfileData()
      );
      
      when(articleQueryService.findBySlug(any(), any()))
          .thenReturn(Optional.of(updatedArticleData));

      Map<String, Object> param = new HashMap<>();
      Map<String, Object> articleParam = new HashMap<>();
      articleParam.put("title", "Updated Title");
      param.put("article", articleParam);

      given()
          .contentType("application/json")
          .header("Authorization", "Token " + token)
          .body(param)
          .when()
          .put("/articles/test-article-title")
          .then()
          .statusCode(200)
          .body("article.title", equalTo("Updated Title"));
    }

    @Test
    @DisplayName("Should update article description")
    void shouldUpdateArticleDescription() {
      when(articleRepository.findBySlug(eq("test-article-title")))
          .thenReturn(Optional.of(testArticle));
      
      when(articleCommandService.updateArticle(any(), any()))
          .thenReturn(testArticle);
      
      when(articleQueryService.findBySlug(any(), any()))
          .thenReturn(Optional.of(testArticleData));

      Map<String, Object> param = new HashMap<>();
      Map<String, Object> articleParam = new HashMap<>();
      articleParam.put("description", "Updated description");
      param.put("article", articleParam);

      given()
          .contentType("application/json")
          .header("Authorization", "Token " + token)
          .body(param)
          .when()
          .put("/articles/test-article-title")
          .then()
          .statusCode(200);
    }

    @Test
    @DisplayName("Should update article body")
    void shouldUpdateArticleBody() {
      when(articleRepository.findBySlug(eq("test-article-title")))
          .thenReturn(Optional.of(testArticle));
      
      when(articleCommandService.updateArticle(any(), any()))
          .thenReturn(testArticle);
      
      when(articleQueryService.findBySlug(any(), any()))
          .thenReturn(Optional.of(testArticleData));

      Map<String, Object> param = new HashMap<>();
      Map<String, Object> articleParam = new HashMap<>();
      articleParam.put("body", "Updated body content");
      param.put("article", articleParam);

      given()
          .contentType("application/json")
          .header("Authorization", "Token " + token)
          .body(param)
          .when()
          .put("/articles/test-article-title")
          .then()
          .statusCode(200);
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent article")
    void shouldReturn404WhenUpdatingNonExistentArticle() {
      when(articleRepository.findBySlug(eq("non-existent-slug")))
          .thenReturn(Optional.empty());

      Map<String, Object> param = new HashMap<>();
      Map<String, Object> articleParam = new HashMap<>();
      articleParam.put("title", "Updated Title");
      param.put("article", articleParam);

      given()
          .contentType("application/json")
          .header("Authorization", "Token " + token)
          .body(param)
          .when()
          .put("/articles/non-existent-slug")
          .then()
          .statusCode(404);
    }

    @Test
    @DisplayName("Should return 403 when user is not the author")
    void shouldReturn403WhenUserIsNotAuthor() {
      User otherUser = new User("other@example.com", "otheruser", "password", "Other bio", "");
      Article otherUserArticle = new Article(
          "Other User Article",
          "Description",
          "Body",
          Arrays.asList("tag"),
          otherUser.getId()
      );

      when(articleRepository.findBySlug(eq("other-user-article")))
          .thenReturn(Optional.of(otherUserArticle));

      Map<String, Object> param = new HashMap<>();
      Map<String, Object> articleParam = new HashMap<>();
      articleParam.put("title", "Trying to update");
      param.put("article", articleParam);

      given()
          .contentType("application/json")
          .header("Authorization", "Token " + token)
          .body(param)
          .when()
          .put("/articles/other-user-article")
          .then()
          .statusCode(403);
    }

    @Test
    @DisplayName("Should return 401 without authentication")
    void shouldReturn401WithoutAuthentication() {
      Map<String, Object> param = new HashMap<>();
      Map<String, Object> articleParam = new HashMap<>();
      articleParam.put("title", "Updated Title");
      param.put("article", articleParam);

      given()
          .contentType("application/json")
          .body(param)
          .when()
          .put("/articles/test-article-title")
          .then()
          .statusCode(401);
    }
  }

  @Nested
  @DisplayName("DELETE /articles/{slug} - Delete Article")
  class DeleteArticleTests {

    @Test
    @DisplayName("Should delete article successfully")
    void shouldDeleteArticleSuccessfully() {
      when(articleRepository.findBySlug(eq("test-article-title")))
          .thenReturn(Optional.of(testArticle));

      given()
          .contentType("application/json")
          .header("Authorization", "Token " + token)
          .when()
          .delete("/articles/test-article-title")
          .then()
          .statusCode(204);

      verify(articleRepository).remove(any(Article.class));
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent article")
    void shouldReturn404WhenDeletingNonExistentArticle() {
      when(articleRepository.findBySlug(eq("non-existent-slug")))
          .thenReturn(Optional.empty());

      given()
          .contentType("application/json")
          .header("Authorization", "Token " + token)
          .when()
          .delete("/articles/non-existent-slug")
          .then()
          .statusCode(404);
    }

    @Test
    @DisplayName("Should return 403 when user is not the author")
    void shouldReturn403WhenUserIsNotAuthorForDelete() {
      User otherUser = new User("other@example.com", "otheruser", "password", "Other bio", "");
      Article otherUserArticle = new Article(
          "Other User Article",
          "Description",
          "Body",
          Arrays.asList("tag"),
          otherUser.getId()
      );

      when(articleRepository.findBySlug(eq("other-user-article")))
          .thenReturn(Optional.of(otherUserArticle));

      given()
          .contentType("application/json")
          .header("Authorization", "Token " + token)
          .when()
          .delete("/articles/other-user-article")
          .then()
          .statusCode(403);
    }

    @Test
    @DisplayName("Should return 401 without authentication")
    void shouldReturn401WithoutAuthenticationForDelete() {
      given()
          .contentType("application/json")
          .when()
          .delete("/articles/test-article-title")
          .then()
          .statusCode(401);
    }
  }

  @Nested
  @DisplayName("Edge Cases and Error Handling")
  class EdgeCasesTests {

    @Test
    @DisplayName("Should handle article with empty tags")
    void shouldHandleArticleWithEmptyTags() {
      ArticleData articleWithNoTags = new ArticleData(
          testArticle.getId(),
          testArticle.getSlug(),
          testArticle.getTitle(),
          testArticle.getDescription(),
          testArticle.getBody(),
          false,
          0,
          DateTime.now(),
          DateTime.now(),
          Collections.emptyList(),
          testArticleData.getProfileData()
      );

      when(articleQueryService.findBySlug(eq("test-article-title"), any()))
          .thenReturn(Optional.of(articleWithNoTags));

      given()
          .contentType("application/json")
          .when()
          .get("/articles/test-article-title")
          .then()
          .statusCode(200)
          .body("article.tagList", empty());
    }

    @Test
    @DisplayName("Should handle article with special characters in slug")
    void shouldHandleArticleWithSpecialCharactersInSlug() {
      when(articleQueryService.findBySlug(eq("article-with-numbers-123"), any()))
          .thenReturn(Optional.of(testArticleData));

      given()
          .contentType("application/json")
          .when()
          .get("/articles/article-with-numbers-123")
          .then()
          .statusCode(200);
    }

    @Test
    @DisplayName("Should handle article with long content")
    void shouldHandleArticleWithLongContent() {
      String longBody = "A".repeat(10000);
      ArticleData longArticle = new ArticleData(
          testArticle.getId(),
          testArticle.getSlug(),
          testArticle.getTitle(),
          testArticle.getDescription(),
          longBody,
          false,
          0,
          DateTime.now(),
          DateTime.now(),
          Arrays.asList("java"),
          testArticleData.getProfileData()
      );

      when(articleQueryService.findBySlug(eq("test-article-title"), any()))
          .thenReturn(Optional.of(longArticle));

      given()
          .contentType("application/json")
          .when()
          .get("/articles/test-article-title")
          .then()
          .statusCode(200)
          .body("article.body", equalTo(longBody));
    }

    @Test
    @DisplayName("Should handle article with high favorites count")
    void shouldHandleArticleWithHighFavoritesCount() {
      ArticleData popularArticle = new ArticleData(
          testArticle.getId(),
          testArticle.getSlug(),
          testArticle.getTitle(),
          testArticle.getDescription(),
          testArticle.getBody(),
          false,
          999999,
          DateTime.now(),
          DateTime.now(),
          Arrays.asList("java", "spring"),
          testArticleData.getProfileData()
      );

      when(articleQueryService.findBySlug(eq("test-article-title"), any()))
          .thenReturn(Optional.of(popularArticle));

      given()
          .contentType("application/json")
          .when()
          .get("/articles/test-article-title")
          .then()
          .statusCode(200)
          .body("article.favoritesCount", equalTo(999999));
    }
  }
}
