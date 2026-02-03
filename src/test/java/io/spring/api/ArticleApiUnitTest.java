package io.spring.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleApi Unit Tests")
class ArticleApiUnitTest {

  @Mock private ArticleQueryService articleQueryService;

  @Mock private ArticleRepository articleRepository;

  @Mock private ArticleCommandService articleCommandService;

  @InjectMocks private ArticleApi articleApi;

  private User user;
  private User anotherUser;
  private Article article;
  private ArticleData articleData;
  private String slug;
  private List<String> tagList;
  private DateTime createdAt;

  @BeforeEach
  void setUp() {
    user = new User("john@example.com", "johndoe", "password123", "bio", "image.jpg");
    anotherUser = new User("jane@example.com", "janedoe", "password456", "bio2", "image2.jpg");
    tagList = Arrays.asList("java", "spring", "testing");
    createdAt = new DateTime();
    article = new Article("Test Article Title", "Description", "Body content", tagList, user.getId(), createdAt);
    slug = article.getSlug();

    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    articleData =
        new ArticleData(
            article.getId(),
            article.getSlug(),
            article.getTitle(),
            article.getDescription(),
            article.getBody(),
            false,
            0,
            createdAt,
            createdAt,
            tagList,
            profileData);
  }

  @Nested
  @DisplayName("GET /articles/{slug} - Get Article")
  class GetArticleTests {

    @Test
    @DisplayName("Should return article successfully when article exists")
    void shouldReturnArticleSuccessfully() {
      when(articleQueryService.findBySlug(eq(slug), eq(user))).thenReturn(Optional.of(articleData));

      ResponseEntity<?> response = articleApi.article(slug, user);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      assertTrue(response.getBody() instanceof Map);
      @SuppressWarnings("unchecked")
      Map<String, Object> body = (Map<String, Object>) response.getBody();
      assertEquals(articleData, body.get("article"));
      verify(articleQueryService).findBySlug(eq(slug), eq(user));
    }

    @Test
    @DisplayName("Should return article for unauthenticated user")
    void shouldReturnArticleForUnauthenticatedUser() {
      when(articleQueryService.findBySlug(eq(slug), eq(null))).thenReturn(Optional.of(articleData));

      ResponseEntity<?> response = articleApi.article(slug, null);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      verify(articleQueryService).findBySlug(eq(slug), eq(null));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when article not found")
    void shouldThrowResourceNotFoundExceptionWhenArticleNotFound() {
      when(articleQueryService.findBySlug(eq(slug), eq(user))).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> articleApi.article(slug, user));
      verify(articleQueryService).findBySlug(eq(slug), eq(user));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent slug")
    void shouldThrowResourceNotFoundExceptionForNonExistentSlug() {
      String nonExistentSlug = "non-existent-article";
      when(articleQueryService.findBySlug(eq(nonExistentSlug), any()))
          .thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class, () -> articleApi.article(nonExistentSlug, user));
    }
  }

  @Nested
  @DisplayName("PUT /articles/{slug} - Update Article")
  class UpdateArticleTests {

    private UpdateArticleParam updateParam;
    private Article updatedArticle;
    private ArticleData updatedArticleData;

    @BeforeEach
    void setUpUpdateTests() {
      updateParam = new UpdateArticleParam("New Title", "New Body", "New Description");
      updatedArticle =
          new Article("New Title", "New Description", "New Body", tagList, user.getId(), createdAt);

      ProfileData profileData =
          new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
      updatedArticleData =
          new ArticleData(
              updatedArticle.getId(),
              updatedArticle.getSlug(),
              updatedArticle.getTitle(),
              updatedArticle.getDescription(),
              updatedArticle.getBody(),
              false,
              0,
              createdAt,
              new DateTime(),
              tagList,
              profileData);
    }

    @Test
    @DisplayName("Should update article successfully when user is the author")
    void shouldUpdateArticleSuccessfully() {
      when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
      when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
          .thenReturn(updatedArticle);
      when(articleQueryService.findBySlug(eq(updatedArticle.getSlug()), eq(user)))
          .thenReturn(Optional.of(updatedArticleData));

      ResponseEntity<?> response = articleApi.updateArticle(slug, user, updateParam);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertNotNull(response.getBody());
      @SuppressWarnings("unchecked")
      Map<String, Object> body = (Map<String, Object>) response.getBody();
      assertEquals(updatedArticleData, body.get("article"));
      verify(articleRepository).findBySlug(eq(slug));
      verify(articleCommandService).updateArticle(eq(article), any(UpdateArticleParam.class));
      verify(articleQueryService).findBySlug(eq(updatedArticle.getSlug()), eq(user));
    }

    @Test
    @DisplayName("Should throw NoAuthorizationException when user is not the author")
    void shouldThrowNoAuthorizationExceptionWhenNotAuthor() {
      Article articleByAnotherUser =
          new Article(
              "Another Article", "Desc", "Body", tagList, anotherUser.getId(), createdAt);
      String anotherSlug = articleByAnotherUser.getSlug();

      when(articleRepository.findBySlug(eq(anotherSlug)))
          .thenReturn(Optional.of(articleByAnotherUser));

      assertThrows(
          NoAuthorizationException.class,
          () -> articleApi.updateArticle(anotherSlug, user, updateParam));
      verify(articleRepository).findBySlug(eq(anotherSlug));
      verifyNoInteractions(articleCommandService);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when article to update not found")
    void shouldThrowResourceNotFoundExceptionWhenArticleToUpdateNotFound() {
      when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class, () -> articleApi.updateArticle(slug, user, updateParam));
      verify(articleRepository).findBySlug(eq(slug));
      verifyNoInteractions(articleCommandService);
      verifyNoInteractions(articleQueryService);
    }

    @Test
    @DisplayName("Should update article with partial data")
    void shouldUpdateArticleWithPartialData() {
      UpdateArticleParam partialUpdateParam = new UpdateArticleParam("Only Title Updated", "", "");

      when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
      when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
          .thenReturn(updatedArticle);
      when(articleQueryService.findBySlug(eq(updatedArticle.getSlug()), eq(user)))
          .thenReturn(Optional.of(updatedArticleData));

      ResponseEntity<?> response = articleApi.updateArticle(slug, user, partialUpdateParam);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      verify(articleCommandService).updateArticle(eq(article), any(UpdateArticleParam.class));
    }
  }

  @Nested
  @DisplayName("DELETE /articles/{slug} - Delete Article")
  class DeleteArticleTests {

    @Test
    @DisplayName("Should delete article successfully when user is the author")
    void shouldDeleteArticleSuccessfully() {
      when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

      ResponseEntity response = articleApi.deleteArticle(slug, user);

      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
      verify(articleRepository).findBySlug(eq(slug));
      verify(articleRepository).remove(eq(article));
    }

    @Test
    @DisplayName("Should throw NoAuthorizationException when user is not the author")
    void shouldThrowNoAuthorizationExceptionWhenNotAuthorForDelete() {
      Article articleByAnotherUser =
          new Article(
              "Another Article", "Desc", "Body", tagList, anotherUser.getId(), createdAt);
      String anotherSlug = articleByAnotherUser.getSlug();

      when(articleRepository.findBySlug(eq(anotherSlug)))
          .thenReturn(Optional.of(articleByAnotherUser));

      assertThrows(
          NoAuthorizationException.class, () -> articleApi.deleteArticle(anotherSlug, user));
      verify(articleRepository).findBySlug(eq(anotherSlug));
      verify(articleRepository, never()).remove(any(Article.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when article to delete not found")
    void shouldThrowResourceNotFoundExceptionWhenArticleToDeleteNotFound() {
      when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> articleApi.deleteArticle(slug, user));
      verify(articleRepository).findBySlug(eq(slug));
      verify(articleRepository, never()).remove(any(Article.class));
    }

    @Test
    @DisplayName("Should not delete article when authorization fails")
    void shouldNotDeleteArticleWhenAuthorizationFails() {
      Article articleByAnotherUser =
          new Article("Title", "Desc", "Body", tagList, anotherUser.getId(), createdAt);
      String anotherSlug = articleByAnotherUser.getSlug();

      when(articleRepository.findBySlug(eq(anotherSlug)))
          .thenReturn(Optional.of(articleByAnotherUser));

      assertThrows(
          NoAuthorizationException.class, () -> articleApi.deleteArticle(anotherSlug, user));
      verify(articleRepository, never()).remove(any());
    }
  }

  @Nested
  @DisplayName("Edge Cases and Boundary Tests")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle article with empty tag list")
    void shouldHandleArticleWithEmptyTagList() {
      Article articleWithNoTags =
          new Article("No Tags Article", "Desc", "Body", Arrays.asList(), user.getId(), createdAt);
      String noTagsSlug = articleWithNoTags.getSlug();

      ProfileData profileData =
          new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
      ArticleData articleDataWithNoTags =
          new ArticleData(
              articleWithNoTags.getId(),
              articleWithNoTags.getSlug(),
              articleWithNoTags.getTitle(),
              articleWithNoTags.getDescription(),
              articleWithNoTags.getBody(),
              false,
              0,
              createdAt,
              createdAt,
              Arrays.asList(),
              profileData);

      when(articleQueryService.findBySlug(eq(noTagsSlug), eq(user)))
          .thenReturn(Optional.of(articleDataWithNoTags));

      ResponseEntity<?> response = articleApi.article(noTagsSlug, user);

      assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle article with special characters in slug")
    void shouldHandleArticleWithSpecialCharactersInSlug() {
      String specialSlug = "test-article-with-special-chars";
      when(articleQueryService.findBySlug(eq(specialSlug), eq(user)))
          .thenReturn(Optional.of(articleData));

      ResponseEntity<?> response = articleApi.article(specialSlug, user);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      verify(articleQueryService).findBySlug(eq(specialSlug), eq(user));
    }

    @Test
    @DisplayName("Should handle favorited article data")
    void shouldHandleFavoritedArticleData() {
      ProfileData profileData =
          new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
      ArticleData favoritedArticleData =
          new ArticleData(
              article.getId(),
              article.getSlug(),
              article.getTitle(),
              article.getDescription(),
              article.getBody(),
              true,
              5,
              createdAt,
              createdAt,
              tagList,
              profileData);

      when(articleQueryService.findBySlug(eq(slug), eq(user)))
          .thenReturn(Optional.of(favoritedArticleData));

      ResponseEntity<?> response = articleApi.article(slug, user);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      @SuppressWarnings("unchecked")
      Map<String, Object> body = (Map<String, Object>) response.getBody();
      ArticleData returnedData = (ArticleData) body.get("article");
      assertTrue(returnedData.isFavorited());
      assertEquals(5, returnedData.getFavoritesCount());
    }

    @Test
    @DisplayName("Should handle article with following author")
    void shouldHandleArticleWithFollowingAuthor() {
      ProfileData followingProfileData =
          new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), true);
      ArticleData articleWithFollowingAuthor =
          new ArticleData(
              article.getId(),
              article.getSlug(),
              article.getTitle(),
              article.getDescription(),
              article.getBody(),
              false,
              0,
              createdAt,
              createdAt,
              tagList,
              followingProfileData);

      when(articleQueryService.findBySlug(eq(slug), eq(user)))
          .thenReturn(Optional.of(articleWithFollowingAuthor));

      ResponseEntity<?> response = articleApi.article(slug, user);

      assertEquals(HttpStatus.OK, response.getStatusCode());
      @SuppressWarnings("unchecked")
      Map<String, Object> body = (Map<String, Object>) response.getBody();
      ArticleData returnedData = (ArticleData) body.get("article");
      assertTrue(returnedData.getProfileData().isFollowing());
    }
  }

  @Nested
  @DisplayName("Verification Tests")
  class VerificationTests {

    @Test
    @DisplayName("Should verify articleQueryService is called with correct parameters for get")
    void shouldVerifyArticleQueryServiceCalledCorrectly() {
      when(articleQueryService.findBySlug(eq(slug), eq(user))).thenReturn(Optional.of(articleData));

      articleApi.article(slug, user);

      verify(articleQueryService).findBySlug(slug, user);
    }

    @Test
    @DisplayName("Should verify articleRepository is called with correct parameters for delete")
    void shouldVerifyArticleRepositoryCalledCorrectly() {
      when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));

      articleApi.deleteArticle(slug, user);

      verify(articleRepository).findBySlug(slug);
      verify(articleRepository).remove(article);
    }

    @Test
    @DisplayName("Should verify articleCommandService is called with correct parameters for update")
    void shouldVerifyArticleCommandServiceCalledCorrectly() {
      UpdateArticleParam updateParam = new UpdateArticleParam("New Title", "New Body", "New Desc");
      Article updatedArticle =
          new Article("New Title", "New Desc", "New Body", tagList, user.getId(), createdAt);

      ProfileData profileData =
          new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
      ArticleData updatedArticleData =
          new ArticleData(
              updatedArticle.getId(),
              updatedArticle.getSlug(),
              updatedArticle.getTitle(),
              updatedArticle.getDescription(),
              updatedArticle.getBody(),
              false,
              0,
              createdAt,
              createdAt,
              tagList,
              profileData);

      when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(article));
      when(articleCommandService.updateArticle(eq(article), eq(updateParam)))
          .thenReturn(updatedArticle);
      when(articleQueryService.findBySlug(eq(updatedArticle.getSlug()), eq(user)))
          .thenReturn(Optional.of(updatedArticleData));

      articleApi.updateArticle(slug, user, updateParam);

      verify(articleCommandService).updateArticle(article, updateParam);
    }
  }
}
