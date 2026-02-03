package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;

  @Mock private ArticleFavoriteRepository articleFavoriteRepository;

  @Mock private ArticleRepository articleRepository;

  private ArticleMutation articleMutation;

  private User testUser;
  private Article testArticle;

  @BeforeEach
  void setUp() {
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    testUser = new User("test@example.com", "testuser", "password", "bio", "image");
    testArticle =
        new Article("Test Title", "description", "body", Arrays.asList("java"), testUser.getId());
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null));
  }

  @Test
  void createArticle_success() {
    authenticateUser();
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("description")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    when(articleCommandService.createArticle(any(), eq(testUser))).thenReturn(testArticle);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(testArticle, result.getLocalContext());
    verify(articleCommandService).createArticle(any(), eq(testUser));
  }

  @Test
  void createArticle_withNullTagList() {
    authenticateUser();
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("description")
            .body("body")
            .build();

    when(articleCommandService.createArticle(any(), eq(testUser))).thenReturn(testArticle);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(testUser));
  }

  @Test
  void createArticle_notAuthenticated() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("description")
            .body("body")
            .build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void updateArticle_success() {
    authenticateUser();
    String slug = "test-title";
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("Updated Title")
            .description("updated description")
            .body("updated body")
            .build();

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(testArticle));
    when(articleCommandService.updateArticle(eq(testArticle), any())).thenReturn(testArticle);

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle(slug, input);

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(articleCommandService).updateArticle(eq(testArticle), any());
  }

  @Test
  void updateArticle_articleNotFound() {
    authenticateUser();
    String slug = "nonexistent";
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("Updated Title").build();

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.updateArticle(slug, input));
  }

  @Test
  void updateArticle_notAuthorized() {
    User anotherUser = new User("other@example.com", "other", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(anotherUser, null));

    String slug = "test-title";
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("Updated Title").build();

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(testArticle));

    assertThrows(NoAuthorizationException.class, () -> articleMutation.updateArticle(slug, input));
  }

  @Test
  void favoriteArticle_success() {
    authenticateUser();
    String slug = "test-title";

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(testArticle));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle(slug);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(testArticle, result.getLocalContext());
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void favoriteArticle_notAuthenticated() {
    String slug = "test-title";

    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle(slug));
  }

  @Test
  void favoriteArticle_articleNotFound() {
    authenticateUser();
    String slug = "nonexistent";

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.favoriteArticle(slug));
  }

  @Test
  void unfavoriteArticle_success() {
    authenticateUser();
    String slug = "test-title";
    ArticleFavorite favorite = new ArticleFavorite(testArticle.getId(), testUser.getId());

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(testArticle));
    when(articleFavoriteRepository.find(eq(testArticle.getId()), eq(testUser.getId())))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle(slug);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(testArticle, result.getLocalContext());
    verify(articleFavoriteRepository).remove(eq(favorite));
  }

  @Test
  void unfavoriteArticle_notFavorited() {
    authenticateUser();
    String slug = "test-title";

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(testArticle));
    when(articleFavoriteRepository.find(eq(testArticle.getId()), eq(testUser.getId())))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle(slug);

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void deleteArticle_success() {
    authenticateUser();
    String slug = "test-title";

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(testArticle));

    DeletionStatus result = articleMutation.deleteArticle(slug);

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(articleRepository).remove(eq(testArticle));
  }

  @Test
  void deleteArticle_notAuthorized() {
    User anotherUser = new User("other@example.com", "other", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(anotherUser, null));

    String slug = "test-title";

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(testArticle));

    assertThrows(NoAuthorizationException.class, () -> articleMutation.deleteArticle(slug));
  }

  @Test
  void deleteArticle_articleNotFound() {
    authenticateUser();
    String slug = "nonexistent";

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.deleteArticle(slug));
  }
}
