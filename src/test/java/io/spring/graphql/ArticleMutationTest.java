package io.spring.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class ArticleMutationTest {

  private ArticleCommandService articleCommandService;
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleRepository articleRepository;
  private ArticleMutation articleMutation;
  private User user;

  @BeforeEach
  public void setUp() {
    articleCommandService = mock(ArticleCommandService.class);
    articleFavoriteRepository = mock(ArticleFavoriteRepository.class);
    articleRepository = mock(ArticleRepository.class);
    articleMutation = new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser(User user) {
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  public void should_create_article_successfully() {
    setAuthenticatedUser(user);
    CreateArticleInput input = CreateArticleInput.newBuilder()
        .title("Test Title")
        .description("Test Description")
        .body("Test Body")
        .tagList(Arrays.asList("tag1", "tag2"))
        .build();

    Article article = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList("tag1", "tag2"), user.getId());
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
    assertThat(result.getLocalContext(), is(article));
  }

  @Test
  public void should_create_article_with_null_taglist() {
    setAuthenticatedUser(user);
    CreateArticleInput input = CreateArticleInput.newBuilder()
        .title("Test Title")
        .description("Test Description")
        .body("Test Body")
        .build();

    Article article = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList(), user.getId());
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertThat(result, is(notNullValue()));
  }

  @Test
  public void should_throw_exception_when_create_article_without_auth() {
    CreateArticleInput input = CreateArticleInput.newBuilder()
        .title("Test Title")
        .description("Test Description")
        .body("Test Body")
        .build();

    try {
      articleMutation.createArticle(input);
      assertThat("Should have thrown exception", false);
    } catch (AuthenticationException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_update_article_successfully() {
    setAuthenticatedUser(user);
    Article article = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList(), user.getId());
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
        .thenReturn(article);

    UpdateArticleInput input = UpdateArticleInput.newBuilder()
        .title("New Title")
        .description("New Description")
        .body("New Body")
        .build();

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("test-slug", input);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_throw_exception_when_update_nonexistent_article() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    UpdateArticleInput input = UpdateArticleInput.newBuilder()
        .title("New Title")
        .build();

    try {
      articleMutation.updateArticle("nonexistent", input);
      assertThat("Should have thrown exception", false);
    } catch (ResourceNotFoundException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_update_article_without_auth() {
    Article article = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList(), "other-user-id");
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    UpdateArticleInput input = UpdateArticleInput.newBuilder()
        .title("New Title")
        .build();

    try {
      articleMutation.updateArticle("test-slug", input);
      assertThat("Should have thrown exception", false);
    } catch (AuthenticationException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_update_article_not_owned() {
    User otherUser = new User("other@test.com", "otheruser", "password", "bio", "image");
    setAuthenticatedUser(otherUser);
    Article article = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList(), user.getId());
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    UpdateArticleInput input = UpdateArticleInput.newBuilder()
        .title("New Title")
        .build();

    try {
      articleMutation.updateArticle("test-slug", input);
      assertThat("Should have thrown exception", false);
    } catch (NoAuthorizationException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_favorite_article_successfully() {
    setAuthenticatedUser(user);
    Article article = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList(), user.getId());
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("test-slug");

    assertThat(result, is(notNullValue()));
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  public void should_throw_exception_when_favorite_without_auth() {
    try {
      articleMutation.favoriteArticle("test-slug");
      assertThat("Should have thrown exception", false);
    } catch (AuthenticationException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_favorite_nonexistent_article() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    try {
      articleMutation.favoriteArticle("nonexistent");
      assertThat("Should have thrown exception", false);
    } catch (ResourceNotFoundException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_unfavorite_article_successfully() {
    setAuthenticatedUser(user);
    Article article = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList(), user.getId());
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-slug");

    assertThat(result, is(notNullValue()));
    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  public void should_unfavorite_article_when_not_favorited() {
    setAuthenticatedUser(user);
    Article article = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList(), user.getId());
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-slug");

    assertThat(result, is(notNullValue()));
  }

  @Test
  public void should_delete_article_successfully() {
    setAuthenticatedUser(user);
    Article article = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList(), user.getId());
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("test-slug");

    assertThat(result, is(notNullValue()));
    assertThat(result.getSuccess(), is(true));
    verify(articleRepository).remove(article);
  }

  @Test
  public void should_throw_exception_when_delete_without_auth() {
    try {
      articleMutation.deleteArticle("test-slug");
      assertThat("Should have thrown exception", false);
    } catch (AuthenticationException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_delete_article_not_owned() {
    User otherUser = new User("other@test.com", "otheruser", "password", "bio", "image");
    setAuthenticatedUser(otherUser);
    Article article = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList(), user.getId());
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    try {
      articleMutation.deleteArticle("test-slug");
      assertThat("Should have thrown exception", false);
    } catch (NoAuthorizationException e) {
      assertThat(e, is(notNullValue()));
    }
  }
}
