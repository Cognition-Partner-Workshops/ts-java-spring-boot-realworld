package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import io.spring.graphql.types.Profile;
import java.util.Arrays;
import java.util.Collections;
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
public class ArticleDatafetcherTest {

  @Mock private ArticleQueryService articleQueryService;

  @Mock private UserRepository userRepository;

  @Mock private DgsDataFetchingEnvironment dfe;

  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ArticleDatafetcher articleDatafetcher;

  private User testUser;
  private ArticleData testArticleData;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    articleDatafetcher = new ArticleDatafetcher(articleQueryService, userRepository);
    testUser = new User("test@example.com", "testuser", "password", "bio", "image");
    profileData = new ProfileData(testUser.getId(), testUser.getUsername(), "bio", "image", false);
    testArticleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "description",
            "body",
            false,
            0,
            DateTime.now(),
            DateTime.now(),
            Arrays.asList("java", "spring"),
            profileData);
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null));
  }

  @Test
  void getFeed_withFirst() {
    authenticateUser();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(testArticleData), Direction.NEXT, true);

    when(articleQueryService.findUserFeedWithCursor(eq(testUser), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void getFeed_withLast() {
    authenticateUser();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(testArticleData), Direction.PREV, true);

    when(articleQueryService.findUserFeedWithCursor(eq(testUser), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(null, null, 10, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void getFeed_withoutFirstOrLast() {
    authenticateUser();

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getFeed(null, null, null, null, dfe));
  }

  @Test
  void getArticles_withFirst() {
    authenticateUser();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(testArticleData), Direction.NEXT, true);

    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), any(), any(CursorPageParameter.class), eq(testUser)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void getArticles_withLast() {
    authenticateUser();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(testArticleData), Direction.PREV, true);

    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), any(), any(CursorPageParameter.class), eq(testUser)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(null, null, 10, null, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void getArticles_withFilters() {
    authenticateUser();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(testArticleData), Direction.NEXT, true);

    when(articleQueryService.findRecentArticlesWithCursor(
            eq("java"), eq("testuser"), eq("favoriter"), any(CursorPageParameter.class), eq(testUser)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getArticles(10, null, null, null, "testuser", "favoriter", "java", dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void getArticles_withoutFirstOrLast() {
    authenticateUser();

    assertThrows(
        IllegalArgumentException.class,
        () -> articleDatafetcher.getArticles(null, null, null, null, null, null, null, dfe));
  }

  @Test
  void getArticle_success() {
    authenticateUser();
    io.spring.core.article.Article coreArticle =
        new io.spring.core.article.Article(
            "Test Title", "description", "body", Arrays.asList("java"), testUser.getId());

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(coreArticle);
    when(articleQueryService.findById(eq(coreArticle.getId()), eq(testUser)))
        .thenReturn(Optional.of(testArticleData));

    DataFetcherResult<Article> result = articleDatafetcher.getArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("Test Title", result.getData().getTitle());
    assertEquals("test-slug", result.getData().getSlug());
  }

  @Test
  void findArticleBySlug_success() {
    authenticateUser();
    String slug = "test-slug";

    when(articleQueryService.findBySlug(eq(slug), eq(testUser)))
        .thenReturn(Optional.of(testArticleData));

    DataFetcherResult<Article> result = articleDatafetcher.findArticleBySlug(slug);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("Test Title", result.getData().getTitle());
    assertEquals(slug, result.getData().getSlug());
  }

  @Test
  void getCommentArticle_success() {
    authenticateUser();
    CommentData commentData =
        new CommentData("comment-id", "body", "article-id", DateTime.now(), DateTime.now(), profileData);

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(commentData);
    when(articleQueryService.findById(eq("article-id"), eq(testUser)))
        .thenReturn(Optional.of(testArticleData));

    DataFetcherResult<Article> result =
        articleDatafetcher.getCommentArticle(dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("Test Title", result.getData().getTitle());
  }

  @Test
  void userFeed_withFirst() {
    authenticateUser();
    Profile profile = Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(testArticleData), Direction.NEXT, true);

    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(testUser));
    when(articleQueryService.findUserFeedWithCursor(eq(testUser), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void userFeed_withLast() {
    authenticateUser();
    Profile profile = Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(testArticleData), Direction.PREV, true);

    when(dfe.getSource()).thenReturn(profile);
    when(userRepository.findByUsername(eq("testuser"))).thenReturn(Optional.of(testUser));
    when(articleQueryService.findUserFeedWithCursor(eq(testUser), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFeed(null, null, 10, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void userFavorites_withFirst() {
    authenticateUser();
    Profile profile = Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(testArticleData), Direction.NEXT, true);

    when(dfe.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), any(), eq("testuser"), any(CursorPageParameter.class), eq(testUser)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userFavorites(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void userArticles_withFirst() {
    authenticateUser();
    Profile profile = Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(testArticleData), Direction.NEXT, true);

    when(dfe.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), eq("testuser"), any(), any(CursorPageParameter.class), eq(testUser)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void userArticles_withLast() {
    authenticateUser();
    Profile profile = Profile.newBuilder().username("testuser").build();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Arrays.asList(testArticleData), Direction.PREV, true);

    when(dfe.getSource()).thenReturn(profile);
    when(articleQueryService.findRecentArticlesWithCursor(
            any(), eq("testuser"), any(), any(CursorPageParameter.class), eq(testUser)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.userArticles(null, null, 10, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void getFeed_emptyResult() {
    authenticateUser();
    CursorPager<ArticleData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    when(articleQueryService.findUserFeedWithCursor(eq(testUser), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<ArticlesConnection> result =
        articleDatafetcher.getFeed(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData().getEdges().isEmpty());
  }
}
