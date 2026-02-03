package io.spring.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import io.spring.application.CursorPager.Direction;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class ArticleDatafetcherTest {

  private ArticleQueryService articleQueryService;
  private UserRepository userRepository;
  private ArticleDatafetcher articleDatafetcher;
  private User user;
  private DgsDataFetchingEnvironment dfe;
  private DataFetchingEnvironment dataFetchingEnvironment;

  @BeforeEach
  public void setUp() {
    articleQueryService = mock(ArticleQueryService.class);
    userRepository = mock(UserRepository.class);
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    dfe = mock(DgsDataFetchingEnvironment.class);
    dataFetchingEnvironment = mock(DataFetchingEnvironment.class);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser(User user) {
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private ArticleData createArticleData(String slug) {
    ProfileData profileData = new ProfileData(user.getId(), user.getUsername(), 
        user.getBio(), user.getImage(), false);
    return new ArticleData("article-id", slug, "Test Title", 
        "Description", "Body", false, 0, DateTime.now(), DateTime.now(), 
        Arrays.asList("tag1"), profileData);
  }

  @Test
  public void should_throw_exception_when_get_feed_without_first_or_last() {
    try {
      articleDatafetcher.getFeed(null, null, null, null, dfe);
      assertThat("Should have thrown exception", false);
    } catch (IllegalArgumentException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_get_feed_with_first_parameter() {
    setAuthenticatedUser(user);
    ArticleData articleData = createArticleData("test-slug");
    CursorPager<ArticleData> cursorPager = new CursorPager<>(
        Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result = articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
    assertThat(result.getData().getEdges().size(), is(1));
  }

  @Test
  public void should_get_feed_with_last_parameter() {
    setAuthenticatedUser(user);
    ArticleData articleData = createArticleData("test-slug");
    CursorPager<ArticleData> cursorPager = new CursorPager<>(
        Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result = articleDatafetcher.getFeed(null, null, 10, null, dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_throw_exception_when_get_articles_without_first_or_last() {
    try {
      articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe);
      assertThat("Should have thrown exception", false);
    } catch (IllegalArgumentException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_get_articles_with_first_parameter() {
    setAuthenticatedUser(user);
    ArticleData articleData = createArticleData("test-slug");
    CursorPager<ArticleData> cursorPager = new CursorPager<>(
        Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(CursorPageParameter.class), eq(user)))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result = articleDatafetcher.getArticles(
        10, null, null, null, "author", "favorited", "tag", dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_get_articles_with_last_parameter() {
    setAuthenticatedUser(user);
    ArticleData articleData = createArticleData("test-slug");
    CursorPager<ArticleData> cursorPager = new CursorPager<>(
        Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), any(), any(CursorPageParameter.class), eq(user)))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result = articleDatafetcher.getArticles(
        null, null, 10, null, null, null, null, dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_find_article_by_slug() {
    setAuthenticatedUser(user);
    ArticleData articleData = createArticleData("test-slug");
    when(articleQueryService.findBySlug("test-slug", user)).thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug("test-slug");

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
    assertThat(result.getData().getSlug(), is("test-slug"));
  }

  @Test
  public void should_throw_exception_when_article_not_found_by_slug() {
    setAuthenticatedUser(user);
    when(articleQueryService.findBySlug("nonexistent", user)).thenReturn(Optional.empty());

    try {
      articleDatafetcher.findArticleBySlug("nonexistent");
      assertThat("Should have thrown exception", false);
    } catch (ResourceNotFoundException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_get_article_from_article_payload() {
    setAuthenticatedUser(user);
    io.spring.core.article.Article coreArticle = new io.spring.core.article.Article(
        "Test Title", "Description", "Body", Arrays.asList("tag1"), user.getId());
    ArticleData articleData = createArticleData("test-slug");
    
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);
    when(articleQueryService.findById(coreArticle.getId(), user)).thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dataFetchingEnvironment);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_get_comment_article() {
    setAuthenticatedUser(user);
    ProfileData profileData = new ProfileData(user.getId(), user.getUsername(), 
        user.getBio(), user.getImage(), false);
    CommentData commentData = new CommentData("comment-id", "Comment body", "article-id", 
        DateTime.now(), DateTime.now(), profileData);
    ArticleData articleData = createArticleData("test-slug");
    
    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById("article-id", user)).thenReturn(Optional.of(articleData));

    DataFetcherResult<Article> result = articleDatafetcher.getCommentArticle(dataFetchingEnvironment);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_throw_exception_when_user_feed_without_first_or_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    try {
      articleDatafetcher.userFeed(null, null, null, null, dfe);
      assertThat("Should have thrown exception", false);
    } catch (IllegalArgumentException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_get_user_feed_with_first_parameter() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    
    ArticleData articleData = createArticleData("test-slug");
    CursorPager<ArticleData> cursorPager = new CursorPager<>(
        Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result = articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_get_user_feed_with_last_parameter() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
    
    ArticleData articleData = createArticleData("test-slug");
    CursorPager<ArticleData> cursorPager = new CursorPager<>(
        Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result = articleDatafetcher.userFeed(null, null, 10, null, dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_throw_exception_when_user_feed_user_not_found() {
    Profile profile = Profile.newBuilder().username("nonexistent").build();
    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

    try {
      articleDatafetcher.userFeed(10, null, null, null, dfe);
      assertThat("Should have thrown exception", false);
    } catch (ResourceNotFoundException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_user_favorites_without_first_or_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    try {
      articleDatafetcher.userFavorites(null, null, null, null, dfe);
      assertThat("Should have thrown exception", false);
    } catch (IllegalArgumentException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_get_user_favorites_with_first_parameter() {
    setAuthenticatedUser(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    
    ArticleData articleData = createArticleData("test-slug");
    CursorPager<ArticleData> cursorPager = new CursorPager<>(
        Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), eq("testuser"), any(CursorPageParameter.class), eq(user)))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result = articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_get_user_favorites_with_last_parameter() {
    setAuthenticatedUser(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    
    ArticleData articleData = createArticleData("test-slug");
    CursorPager<ArticleData> cursorPager = new CursorPager<>(
        Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), any(), eq("testuser"), any(CursorPageParameter.class), eq(user)))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result = articleDatafetcher.userFavorites(null, null, 10, null, dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_throw_exception_when_user_articles_without_first_or_last() {
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);

    try {
      articleDatafetcher.userArticles(null, null, null, null, dfe);
      assertThat("Should have thrown exception", false);
    } catch (IllegalArgumentException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_get_user_articles_with_first_parameter() {
    setAuthenticatedUser(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    
    ArticleData articleData = createArticleData("test-slug");
    CursorPager<ArticleData> cursorPager = new CursorPager<>(
        Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), eq("testuser"), any(), any(CursorPageParameter.class), eq(user)))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result = articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_get_user_articles_with_last_parameter() {
    setAuthenticatedUser(user);
    Profile profile = Profile.newBuilder().username("testuser").build();
    when(dfe.getSource()).thenReturn(profile);
    
    ArticleData articleData = createArticleData("test-slug");
    CursorPager<ArticleData> cursorPager = new CursorPager<>(
        Arrays.asList(articleData), Direction.NEXT, false);
    when(articleQueryService.findRecentArticlesWithCursor(any(), eq("testuser"), any(), any(CursorPageParameter.class), eq(user)))
        .thenReturn(cursorPager);

    DataFetcherResult<ArticlesConnection> result = articleDatafetcher.userArticles(null, null, 10, null, dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }
}
