package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.ArticlesConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;
  @Mock private UserRepository userRepository;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;
  @Mock private com.netflix.graphql.dgs.DgsDataFetchingEnvironment dgsDataFetchingEnvironment;

  private ArticleDatafetcher articleDatafetcher;
  private User user;
  private ProfileData profileData;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    user = new User("test@example.com", "testuser", "password123", "bio", "image.jpg");
    profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            0,
            DateTime.now(),
            DateTime.now(),
            Arrays.asList("tag1", "tag2"),
            profileData);
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void getFeed_withFirst_success() {
    setAuthenticatedUser();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void getFeed_withLast_success() {
    setAuthenticatedUser();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void getFeed_withoutFirstOrLast_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dgsDataFetchingEnvironment));
  }

  @Test
  void getArticles_withFirst_success() {
    setAuthenticatedUser();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), any(), any(CursorPageParameter.class), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(
            10, null, null, null, "testuser", null, "java", dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void getArticles_withLast_success() {
    setAuthenticatedUser();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), any(), any(CursorPageParameter.class), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(
            null, null, 10, null, null, null, null, dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void getArticles_withoutFirstOrLast_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            articleDatafetcher.getArticles(
                null, null, null, null, null, null, null, dgsDataFetchingEnvironment));
  }

  @Test
  void findArticleBySlug_success() {
    setAuthenticatedUser();

    when(articleQueryService.findBySlug(eq("test-slug"), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.findArticleBySlug("test-slug");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("test-slug", result.getData().getSlug());
    assertEquals("Test Title", result.getData().getTitle());
  }

  @Test
  void findArticleBySlug_notFound() {
    setAuthenticatedUser();

    when(articleQueryService.findBySlug(eq("nonexistent"), eq(user))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleDatafetcher.findArticleBySlug("nonexistent"));
  }

  @Test
  void getArticle_success() {
    setAuthenticatedUser();

    Article coreArticle =
        new Article(
            "Test Title", "Test Description", "Test Body", Collections.emptyList(), user.getId());

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);
    when(articleQueryService.findById(eq(coreArticle.getId()), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void getArticle_notFound() {
    setAuthenticatedUser();

    Article coreArticle =
        new Article(
            "Test Title", "Test Description", "Test Body", Collections.emptyList(), user.getId());

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);
    when(articleQueryService.findById(eq(coreArticle.getId()), eq(user)))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleDatafetcher.getArticle(dataFetchingEnvironment));
  }

  @Test
  void getCommentArticle_success() {
    setAuthenticatedUser();

    CommentData commentData =
        new CommentData(
            "comment-id",
            "Test body",
            articleData.getId(),
            DateTime.now(),
            DateTime.now(),
            profileData);

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq(articleData.getId()), eq(user)))
        .thenReturn(Optional.of(articleData));

    DataFetcherResult<io.spring.graphql.types.Article> result =
        articleDatafetcher.getCommentArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void userFeed_withFirst_success() {
    setAuthenticatedUser();

    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void userFeed_withLast_success() {
    setAuthenticatedUser();

    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(user));
    when(articleQueryService.findUserFeedWithCursor(eq(user), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 10, null, dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void userFeed_withoutFirstOrLast_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFeed(null, null, null, null, dgsDataFetchingEnvironment));
  }

  @Test
  void userFavorites_withFirst_success() {
    setAuthenticatedUser();

    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), eq("testuser"), any(CursorPageParameter.class), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void userFavorites_withLast_success() {
    setAuthenticatedUser();

    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), eq("testuser"), any(CursorPageParameter.class), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(null, null, 10, null, dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void userFavorites_withoutFirstOrLast_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userFavorites(null, null, null, null, dgsDataFetchingEnvironment));
  }

  @Test
  void userArticles_withFirst_success() {
    setAuthenticatedUser();

    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.NEXT, false);

    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), eq("testuser"), any(), any(CursorPageParameter.class), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void userArticles_withLast_success() {
    setAuthenticatedUser();

    io.spring.graphql.types.Profile profile =
        io.spring.graphql.types.Profile.newBuilder().username("testuser").build();

    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(articleData), Direction.PREV, false);

    when(dgsDataFetchingEnvironment.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), eq("testuser"), any(), any(CursorPageParameter.class), eq(user)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 10, null, dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void userArticles_withoutFirstOrLast_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.userArticles(null, null, null, null, dgsDataFetchingEnvironment));
  }
}
