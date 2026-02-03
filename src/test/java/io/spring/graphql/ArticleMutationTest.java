package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
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
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  private ArticleMutation articleMutation;
  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("test@example.com", "testuser", "password123", "bio", "image.jpg");
    article =
        new Article("Test Title", "Test Description", "Test Body", Arrays.asList("tag1", "tag2"), user.getId());
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void createArticle_success() {
    setAuthenticatedUser();

    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("tag1", "tag2"))
            .build();

    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
  }

  @Test
  void createArticle_withNullTagList() {
    setAuthenticatedUser();

    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(null)
            .build();

    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
  }

  @Test
  void createArticle_notAuthenticated() {
    org.springframework.security.authentication.AnonymousAuthenticationToken auth =
        new org.springframework.security.authentication.AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            java.util.Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void updateArticle_success() {
    setAuthenticatedUser();

    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("Updated Title")
            .description("Updated Description")
            .body("Updated Body")
            .build();

    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle("test-title", input);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void updateArticle_notFound() {
    setAuthenticatedUser();

    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("Updated Title").build();

    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("nonexistent", input));
  }

  @Test
  void updateArticle_notAuthorized() {
    User otherUser = new User("other@example.com", "otheruser", "password", "bio", "image");
    Article otherArticle =
        new Article("Other Title", "Other Desc", "Other Body", Collections.emptyList(), otherUser.getId());

    setAuthenticatedUser();

    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("Updated Title").build();

    when(articleRepository.findBySlug(eq("other-title"))).thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle("other-title", input));
  }

  @Test
  void favoriteArticle_success() {
    setAuthenticatedUser();

    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void favoriteArticle_notAuthenticated() {
    org.springframework.security.authentication.AnonymousAuthenticationToken auth =
        new org.springframework.security.authentication.AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            java.util.Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("test-title"));
  }

  @Test
  void favoriteArticle_articleNotFound() {
    setAuthenticatedUser();

    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("nonexistent"));
  }

  @Test
  void unfavoriteArticle_success() {
    setAuthenticatedUser();

    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(eq(favorite));
  }

  @Test
  void unfavoriteArticle_notFavorited() {
    setAuthenticatedUser();

    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void deleteArticle_success() {
    setAuthenticatedUser();

    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("test-title");

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(articleRepository).remove(eq(article));
  }

  @Test
  void deleteArticle_notAuthorized() {
    User otherUser = new User("other@example.com", "otheruser", "password", "bio", "image");
    Article otherArticle =
        new Article("Other Title", "Other Desc", "Other Body", Collections.emptyList(), otherUser.getId());

    setAuthenticatedUser();

    when(articleRepository.findBySlug(eq("other-title"))).thenReturn(Optional.of(otherArticle));

    assertThrows(NoAuthorizationException.class, () -> articleMutation.deleteArticle("other-title"));
  }

  @Test
  void deleteArticle_notFound() {
    setAuthenticatedUser();

    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.deleteArticle("nonexistent"));
  }
}
