package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  private ArticleMutation articleMutation;

  @BeforeEach
  public void setUp() {
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User user) {
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
            user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(authToken);
  }

  @Test
  public void should_create_article_successfully() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Article")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article = new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  public void should_create_article_with_null_tags() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Article")
            .description("Test Description")
            .body("Test Body")
            .build();

    Article article = new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  public void should_update_article_successfully() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    Article article = new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), user.getId());
    UpdateArticleInput updateInput =
        UpdateArticleInput.newBuilder()
            .title("Updated Title")
            .description("Updated Description")
            .body("Updated Body")
            .build();

    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle("test-article", updateInput);

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  public void should_update_article_with_partial_changes() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    Article article = new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), user.getId());
    UpdateArticleInput updateInput = UpdateArticleInput.newBuilder().title("Updated Title").build();

    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("test-article", updateInput);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  public void should_throw_resource_not_found_when_article_not_found_for_update() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    UpdateArticleInput updateInput = UpdateArticleInput.newBuilder().title("Updated Title").build();
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("nonexistent", updateInput));
  }

  @Test
  public void should_throw_no_authorization_when_user_cannot_update_article() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    User otherUser = new User("other@test.com", "otheruser", "password", "", "");
    authenticateUser(user);

    Article article =
        new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), otherUser.getId());
    UpdateArticleInput updateInput = UpdateArticleInput.newBuilder().title("Updated Title").build();

    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle("test-article", updateInput));
  }

  @Test
  public void should_favorite_article_successfully() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    Article article = new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("test-article");

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  public void should_throw_resource_not_found_when_article_not_found_for_favorite() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("nonexistent"));
  }

  @Test
  public void should_unfavorite_article_successfully() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    Article article = new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), user.getId());
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());

    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-article");

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  public void should_throw_resource_not_found_when_article_not_found_for_unfavorite() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.unfavoriteArticle("nonexistent"));
  }

  @Test
  public void should_delete_article_successfully() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    Article article = new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("test-article");

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  public void should_throw_resource_not_found_when_article_not_found_for_delete() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("nonexistent"));
  }

  @Test
  public void should_throw_no_authorization_when_user_cannot_delete_article() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    User otherUser = new User("other@test.com", "otheruser", "password", "", "");
    authenticateUser(user);

    Article article =
        new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), otherUser.getId());
    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle("test-article"));
  }
}
