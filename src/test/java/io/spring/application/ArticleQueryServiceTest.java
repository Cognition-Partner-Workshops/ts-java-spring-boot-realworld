package io.spring.application;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleQueryServiceTest {

  @Mock
  private ArticleReadService articleReadService;

  @Mock
  private UserRelationshipQueryService userRelationshipQueryService;

  @Mock
  private ArticleFavoritesReadService articleFavoritesReadService;

  private ArticleQueryService articleQueryService;

  @BeforeEach
  public void setUp() {
    articleQueryService = new ArticleQueryService(articleReadService, userRelationshipQueryService, articleFavoritesReadService);
  }

  private ArticleData createArticleData(String id, String slug) {
    ProfileData author = new ProfileData("author-id", "author", "bio", "image", false);
    return new ArticleData(id, slug, "title", "description", "body", false, 0, DateTime.now(), DateTime.now(), Arrays.asList("java"), author);
  }

  @Test
  public void should_find_article_by_id() {
    String articleId = "article-123";
    ArticleData articleData = createArticleData(articleId, "test-article");
    
    when(articleReadService.findById(articleId)).thenReturn(articleData);

    Optional<ArticleData> result = articleQueryService.findById(articleId, null);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getId(), is(articleId));
  }

  @Test
  public void should_return_empty_when_article_not_found_by_id() {
    String articleId = "nonexistent";
    
    when(articleReadService.findById(articleId)).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findById(articleId, null);

    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void should_find_article_by_id_with_user() {
    String articleId = "article-123";
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    ArticleData articleData = createArticleData(articleId, "test-article");
    
    when(articleReadService.findById(articleId)).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(user.getId(), articleId)).thenReturn(true);
    when(articleFavoritesReadService.articleFavoriteCount(articleId)).thenReturn(5);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "author-id")).thenReturn(true);

    Optional<ArticleData> result = articleQueryService.findById(articleId, user);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().isFavorited(), is(true));
    assertThat(result.get().getFavoritesCount(), is(5));
    assertThat(result.get().getProfileData().isFollowing(), is(true));
  }

  @Test
  public void should_find_article_by_slug() {
    String slug = "test-article";
    ArticleData articleData = createArticleData("article-123", slug);
    
    when(articleReadService.findBySlug(slug)).thenReturn(articleData);

    Optional<ArticleData> result = articleQueryService.findBySlug(slug, null);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().getSlug(), is(slug));
  }

  @Test
  public void should_return_empty_when_article_not_found_by_slug() {
    String slug = "nonexistent";
    
    when(articleReadService.findBySlug(slug)).thenReturn(null);

    Optional<ArticleData> result = articleQueryService.findBySlug(slug, null);

    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void should_find_recent_articles() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    ArticleData articleData = createArticleData("article-123", "test-article");
    Page page = new Page(0, 10);
    
    when(articleReadService.queryArticles(null, null, null, page)).thenReturn(Arrays.asList("article-123"));
    when(articleReadService.countArticle(null, null, null)).thenReturn(1);
    when(articleReadService.findArticles(Arrays.asList("article-123"))).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any())).thenReturn(Arrays.asList(new ArticleFavoriteCount("article-123", 5)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>(Arrays.asList("article-123")));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any())).thenReturn(new HashSet<>());

    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, user);

    assertThat(result.getCount(), is(1));
    assertThat(result.getArticleDatas().size(), is(1));
  }

  @Test
  public void should_return_empty_list_when_no_articles() {
    Page page = new Page(0, 10);
    
    when(articleReadService.queryArticles(null, null, null, page)).thenReturn(Collections.emptyList());
    when(articleReadService.countArticle(null, null, null)).thenReturn(0);

    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, null);

    assertThat(result.getCount(), is(0));
    assertThat(result.getArticleDatas().isEmpty(), is(true));
  }

  @Test
  public void should_find_user_feed() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    ArticleData articleData = createArticleData("article-123", "test-article");
    Page page = new Page(0, 10);
    
    when(userRelationshipQueryService.followedUsers(user.getId())).thenReturn(Arrays.asList("followed-user-1"));
    when(articleReadService.findArticlesOfAuthors(Arrays.asList("followed-user-1"), page)).thenReturn(Arrays.asList(articleData));
    when(articleReadService.countFeedSize(Arrays.asList("followed-user-1"))).thenReturn(1);
    when(articleFavoritesReadService.articlesFavoriteCount(any())).thenReturn(Arrays.asList(new ArticleFavoriteCount("article-123", 5)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any())).thenReturn(new HashSet<>());

    ArticleDataList result = articleQueryService.findUserFeed(user, page);

    assertThat(result.getCount(), is(1));
    assertThat(result.getArticleDatas().size(), is(1));
  }

  @Test
  public void should_return_empty_feed_when_no_followed_users() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    Page page = new Page(0, 10);
    
    when(userRelationshipQueryService.followedUsers(user.getId())).thenReturn(Collections.emptyList());

    ArticleDataList result = articleQueryService.findUserFeed(user, page);

    assertThat(result.getCount(), is(0));
    assertThat(result.getArticleDatas().isEmpty(), is(true));
  }

  @Test
  public void should_find_article_by_slug_with_user() {
    String slug = "test-article";
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    ArticleData articleData = createArticleData("article-123", slug);
    
    when(articleReadService.findBySlug(slug)).thenReturn(articleData);
    when(articleFavoritesReadService.isUserFavorite(user.getId(), "article-123")).thenReturn(true);
    when(articleFavoritesReadService.articleFavoriteCount("article-123")).thenReturn(5);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "author-id")).thenReturn(true);

    Optional<ArticleData> result = articleQueryService.findBySlug(slug, user);

    assertThat(result.isPresent(), is(true));
    assertThat(result.get().isFavorited(), is(true));
    assertThat(result.get().getFavoritesCount(), is(5));
    assertThat(result.get().getProfileData().isFollowing(), is(true));
  }

  @Test
  public void should_find_recent_articles_with_cursor() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    ArticleData articleData = createArticleData("article-123", "test-article");
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    
    when(articleReadService.findArticlesWithCursor(null, null, null, page)).thenReturn(Arrays.asList("article-123"));
    when(articleReadService.findArticles(Arrays.asList("article-123"))).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any())).thenReturn(Arrays.asList(new ArticleFavoriteCount("article-123", 5)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>(Arrays.asList("article-123")));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any())).thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertThat(result.getData().size(), is(1));
    assertThat(result.hasNext(), is(false));
  }

  @Test
  public void should_return_empty_cursor_pager_when_no_articles() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    
    when(articleReadService.findArticlesWithCursor(null, null, null, page)).thenReturn(Collections.emptyList());

    CursorPager<ArticleData> result = articleQueryService.findRecentArticlesWithCursor(null, null, null, page, null);

    assertThat(result.getData().isEmpty(), is(true));
    assertThat(result.hasNext(), is(false));
  }

  @Test
  public void should_find_recent_articles_with_cursor_has_extra() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    ArticleData articleData1 = createArticleData("article-1", "test-article-1");
    ArticleData articleData2 = createArticleData("article-2", "test-article-2");
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, CursorPager.Direction.NEXT);
    
    when(articleReadService.findArticlesWithCursor(null, null, null, page)).thenReturn(new ArrayList<>(Arrays.asList("article-1", "article-2")));
    when(articleReadService.findArticles(Arrays.asList("article-1"))).thenReturn(Arrays.asList(articleData1));
    when(articleFavoritesReadService.articlesFavoriteCount(any())).thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 5)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any())).thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertThat(result.getData().size(), is(1));
    assertThat(result.hasNext(), is(true));
  }

  @Test
  public void should_find_recent_articles_with_cursor_prev_direction() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    ArticleData articleData = createArticleData("article-123", "test-article");
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, CursorPager.Direction.PREV);
    
    when(articleReadService.findArticlesWithCursor(null, null, null, page)).thenReturn(Arrays.asList("article-123"));
    when(articleReadService.findArticles(Arrays.asList("article-123"))).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any())).thenReturn(Arrays.asList(new ArticleFavoriteCount("article-123", 5)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any())).thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findRecentArticlesWithCursor(null, null, null, page, user);

    assertThat(result.getData().size(), is(1));
    assertThat(result.hasPrevious(), is(false));
  }

  @Test
  public void should_find_user_feed_with_cursor() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    ArticleData articleData = createArticleData("article-123", "test-article");
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    
    when(userRelationshipQueryService.followedUsers(user.getId())).thenReturn(Arrays.asList("followed-user-1"));
    when(articleReadService.findArticlesOfAuthorsWithCursor(Arrays.asList("followed-user-1"), page)).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any())).thenReturn(Arrays.asList(new ArticleFavoriteCount("article-123", 5)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any())).thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertThat(result.getData().size(), is(1));
    assertThat(result.hasNext(), is(false));
  }

  @Test
  public void should_return_empty_cursor_pager_when_no_followed_users() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);
    
    when(userRelationshipQueryService.followedUsers(user.getId())).thenReturn(Collections.emptyList());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertThat(result.getData().isEmpty(), is(true));
    assertThat(result.hasNext(), is(false));
  }

  @Test
  public void should_find_user_feed_with_cursor_has_extra() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    ArticleData articleData1 = createArticleData("article-1", "test-article-1");
    ArticleData articleData2 = createArticleData("article-2", "test-article-2");
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, CursorPager.Direction.NEXT);
    
    when(userRelationshipQueryService.followedUsers(user.getId())).thenReturn(Arrays.asList("followed-user-1"));
    when(articleReadService.findArticlesOfAuthorsWithCursor(Arrays.asList("followed-user-1"), page)).thenReturn(new ArrayList<>(Arrays.asList(articleData1, articleData2)));
    when(articleFavoritesReadService.articlesFavoriteCount(any())).thenReturn(Arrays.asList(new ArticleFavoriteCount("article-1", 5)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any())).thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertThat(result.getData().size(), is(1));
    assertThat(result.hasNext(), is(true));
  }

  @Test
  public void should_find_user_feed_with_cursor_prev_direction() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    ArticleData articleData = createArticleData("article-123", "test-article");
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, CursorPager.Direction.PREV);
    
    when(userRelationshipQueryService.followedUsers(user.getId())).thenReturn(Arrays.asList("followed-user-1"));
    when(articleReadService.findArticlesOfAuthorsWithCursor(Arrays.asList("followed-user-1"), page)).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any())).thenReturn(Arrays.asList(new ArticleFavoriteCount("article-123", 5)));
    when(articleFavoritesReadService.userFavorites(any(), eq(user))).thenReturn(new HashSet<>());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any())).thenReturn(new HashSet<>());

    CursorPager<ArticleData> result = articleQueryService.findUserFeedWithCursor(user, page);

    assertThat(result.getData().size(), is(1));
    assertThat(result.hasPrevious(), is(false));
  }

  @Test
  public void should_find_recent_articles_without_user() {
    ArticleData articleData = createArticleData("article-123", "test-article");
    Page page = new Page(0, 10);
    
    when(articleReadService.queryArticles(null, null, null, page)).thenReturn(Arrays.asList("article-123"));
    when(articleReadService.countArticle(null, null, null)).thenReturn(1);
    when(articleReadService.findArticles(Arrays.asList("article-123"))).thenReturn(Arrays.asList(articleData));
    when(articleFavoritesReadService.articlesFavoriteCount(any())).thenReturn(Arrays.asList(new ArticleFavoriteCount("article-123", 5)));

    ArticleDataList result = articleQueryService.findRecentArticles(null, null, null, page, null);

    assertThat(result.getCount(), is(1));
    assertThat(result.getArticleDatas().size(), is(1));
  }
}
