package io.spring.application.article;

import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.Page;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({
  ArticleQueryService.class,
  MyBatisUserRepository.class,
  MyBatisArticleRepository.class,
  MyBatisArticleFavoriteRepository.class
})
public class ArticleQueryServiceTest extends DbTestBase {
  @Autowired private ArticleQueryService queryService;

  @Autowired private ArticleRepository articleRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@gmail.com", "aisensiy", "123", "", "");
    userRepository.save(user);
    article =
        new Article(
            "test", "desc", "body", Arrays.asList("java", "spring"), user.getId(), Instant.now());
    articleRepository.save(article);
  }

  @Test
  public void should_fetch_article_success() {
    Optional<ArticleData> optional = queryService.findById(article.getId(), user);
    Assertions.assertTrue(optional.isPresent());

    ArticleData fetched = optional.get();
    Assertions.assertEquals(fetched.getFavoritesCount(), 0);
    Assertions.assertFalse(fetched.isFavorited());
    Assertions.assertNotNull(fetched.getCreatedAt());
    Assertions.assertNotNull(fetched.getUpdatedAt());
    Assertions.assertTrue(fetched.getTagList().contains("java"));
  }

  @Test
  public void should_get_article_with_right_favorite_and_favorite_count() {
    User anotherUser = new User("other@test.com", "other", "123", "", "");
    userRepository.save(anotherUser);
    articleFavoriteRepository.save(new ArticleFavorite(article.getId(), anotherUser.getId()));

    Optional<ArticleData> optional = queryService.findById(article.getId(), anotherUser);
    Assertions.assertTrue(optional.isPresent());

    ArticleData articleData = optional.get();
    Assertions.assertEquals(articleData.getFavoritesCount(), 1);
    Assertions.assertTrue(articleData.isFavorited());
  }

  @Test
  public void should_get_default_article_list() {
    Article anotherArticle =
        new Article(
            "new article",
            "desc",
            "body",
            Arrays.asList("test"),
            user.getId(),
            Instant.now().minus(1, ChronoUnit.HOURS));
    articleRepository.save(anotherArticle);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, null, null, new Page(), user);
    Assertions.assertEquals(recentArticles.getCount(), 2);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 2);
    Assertions.assertEquals(recentArticles.getArticleDatas().get(0).getId(), article.getId());

    ArticleDataList nodata =
        queryService.findRecentArticles(null, null, null, new Page(2, 10), user);
    Assertions.assertEquals(nodata.getCount(), 2);
    Assertions.assertEquals(nodata.getArticleDatas().size(), 0);
  }

  @Test
  public void should_get_default_article_list_by_cursor() {
    Article anotherArticle =
        new Article(
            "new article",
            "desc",
            "body",
            Arrays.asList("test"),
            user.getId(),
            Instant.now().minus(1, ChronoUnit.HOURS));
    articleRepository.save(anotherArticle);

    CursorPager<ArticleData> recentArticles =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 20, Direction.NEXT), user);
    Assertions.assertEquals(recentArticles.getData().size(), 2);
    Assertions.assertEquals(recentArticles.getData().get(0).getId(), article.getId());

    CursorPager<ArticleData> nodata =
        queryService.findRecentArticlesWithCursor(
            null,
            null,
            null,
            new CursorPageParameter<Instant>(
                DateTimeCursor.parse(recentArticles.getEndCursor().toString()), 20, Direction.NEXT),
            user);
    Assertions.assertEquals(nodata.getData().size(), 0);
    Assertions.assertEquals(nodata.getStartCursor(), null);

    CursorPager<ArticleData> prevArticles =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 20, Direction.PREV), user);
    Assertions.assertEquals(prevArticles.getData().size(), 2);
  }

  @Test
  public void should_query_article_by_author() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);

    Article anotherArticle =
        new Article("new article", "desc", "body", Arrays.asList("test"), anotherUser.getId());
    articleRepository.save(anotherArticle);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, user.getUsername(), null, new Page(), user);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 1);
    Assertions.assertEquals(recentArticles.getCount(), 1);
  }

  @Test
  public void should_query_article_by_favorite() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);

    Article anotherArticle =
        new Article("new article", "desc", "body", Arrays.asList("test"), anotherUser.getId());
    articleRepository.save(anotherArticle);

    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), anotherUser.getId());
    articleFavoriteRepository.save(articleFavorite);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(
            null, null, anotherUser.getUsername(), new Page(), anotherUser);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 1);
    Assertions.assertEquals(recentArticles.getCount(), 1);
    ArticleData articleData = recentArticles.getArticleDatas().get(0);
    Assertions.assertEquals(articleData.getId(), article.getId());
    Assertions.assertEquals(articleData.getFavoritesCount(), 1);
    Assertions.assertTrue(articleData.isFavorited());
  }

  @Test
  public void should_query_article_by_tag() {
    Article anotherArticle =
        new Article("new article", "desc", "body", Arrays.asList("test"), user.getId());
    articleRepository.save(anotherArticle);

    ArticleDataList recentArticles =
        queryService.findRecentArticles("spring", null, null, new Page(), user);
    Assertions.assertEquals(recentArticles.getArticleDatas().size(), 1);
    Assertions.assertEquals(recentArticles.getCount(), 1);
    Assertions.assertEquals(recentArticles.getArticleDatas().get(0).getId(), article.getId());

    ArticleDataList notag = queryService.findRecentArticles("notag", null, null, new Page(), user);
    Assertions.assertEquals(notag.getCount(), 0);
  }

  @Test
  public void should_show_following_if_user_followed_author() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);

    FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
    userRepository.saveRelation(followRelation);

    ArticleDataList recentArticles =
        queryService.findRecentArticles(null, null, null, new Page(), anotherUser);
    Assertions.assertEquals(recentArticles.getCount(), 1);
    ArticleData articleData = recentArticles.getArticleDatas().get(0);
    Assertions.assertTrue(articleData.getProfileData().isFollowing());
  }

  @Test
  public void should_get_user_feed() {
    User anotherUser = new User("other@email.com", "other", "123", "", "");
    userRepository.save(anotherUser);

    FollowRelation followRelation = new FollowRelation(anotherUser.getId(), user.getId());
    userRepository.saveRelation(followRelation);

    ArticleDataList userFeed = queryService.findUserFeed(user, new Page());
    Assertions.assertEquals(userFeed.getCount(), 0);

    ArticleDataList anotherUserFeed = queryService.findUserFeed(anotherUser, new Page());
    Assertions.assertEquals(anotherUserFeed.getCount(), 1);
    ArticleData articleData = anotherUserFeed.getArticleDatas().get(0);
    Assertions.assertTrue(articleData.getProfileData().isFollowing());
  }

  @Test
  public void should_page_through_articles_with_cursor_boundaries() {
    Instant base = Instant.parse("2016-02-18T03:22:56.637Z");
    Article oldest = articleAt("oldest", base);
    Article middle = articleAt("middle", base.plusMillis(1));
    Article newest = articleAt("newest", base.plusMillis(2));
    // "article" from setUp (created now) is the most recent one, so 4 rows in total.

    CursorPager<ArticleData> firstPage =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 2, Direction.NEXT), user);
    Assertions.assertEquals(2, firstPage.getData().size());
    Assertions.assertEquals(article.getId(), firstPage.getData().get(0).getId());
    Assertions.assertEquals(newest.getId(), firstPage.getData().get(1).getId());
    Assertions.assertTrue(firstPage.hasNext());
    Assertions.assertFalse(firstPage.hasPrevious());
    Assertions.assertEquals(
        String.valueOf(newest.getCreatedAt().toEpochMilli()), firstPage.getEndCursor().toString());

    CursorPager<ArticleData> lastPage =
        queryService.findRecentArticlesWithCursor(
            null,
            null,
            null,
            new CursorPageParameter<>(
                DateTimeCursor.parse(firstPage.getEndCursor().toString()), 2, Direction.NEXT),
            user);
    Assertions.assertEquals(2, lastPage.getData().size());
    Assertions.assertEquals(middle.getId(), lastPage.getData().get(0).getId());
    Assertions.assertEquals(oldest.getId(), lastPage.getData().get(1).getId());
    Assertions.assertFalse(lastPage.hasNext());
    Assertions.assertFalse(lastPage.hasPrevious());

    CursorPager<ArticleData> beyondLast =
        queryService.findRecentArticlesWithCursor(
            null,
            null,
            null,
            new CursorPageParameter<>(
                DateTimeCursor.parse(lastPage.getEndCursor().toString()), 2, Direction.NEXT),
            user);
    Assertions.assertTrue(beyondLast.getData().isEmpty());
    Assertions.assertNull(beyondLast.getStartCursor());
    Assertions.assertNull(beyondLast.getEndCursor());
    Assertions.assertFalse(beyondLast.hasNext());
    Assertions.assertFalse(beyondLast.hasPrevious());

    CursorPager<ArticleData> previousPage =
        queryService.findRecentArticlesWithCursor(
            null,
            null,
            null,
            new CursorPageParameter<>(
                DateTimeCursor.parse(lastPage.getStartCursor().toString()), 2, Direction.PREV),
            user);
    Assertions.assertEquals(2, previousPage.getData().size());
    Assertions.assertEquals(article.getId(), previousPage.getData().get(0).getId());
    Assertions.assertEquals(newest.getId(), previousPage.getData().get(1).getId());
    Assertions.assertFalse(previousPage.hasNext());
    Assertions.assertFalse(previousPage.hasPrevious());

    CursorPager<ArticleData> previousOfNewest =
        queryService.findRecentArticlesWithCursor(
            null,
            null,
            null,
            new CursorPageParameter<>(newest.getCreatedAt(), 1, Direction.PREV),
            user);
    Assertions.assertEquals(1, previousOfNewest.getData().size());
    Assertions.assertEquals(article.getId(), previousOfNewest.getData().get(0).getId());
    Assertions.assertFalse(previousOfNewest.hasPrevious());
  }

  @Test
  public void should_exclude_rows_with_timestamp_equal_to_cursor() {
    Instant base = Instant.parse("2016-02-18T03:22:56.637Z");
    Article first = articleAt("first", base);
    Article second = articleAt("second", base);
    Article older = articleAt("older", base.minusMillis(1));

    CursorPager<ArticleData> page =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(null, 10, Direction.NEXT), user);
    Assertions.assertEquals(4, page.getData().size());
    Assertions.assertEquals(
        String.valueOf(base.minusMillis(1).toEpochMilli()), page.getEndCursor().toString());

    CursorPager<ArticleData> afterBase =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(base, 10, Direction.NEXT), user);
    Assertions.assertEquals(1, afterBase.getData().size());
    Assertions.assertEquals(older.getId(), afterBase.getData().get(0).getId());

    CursorPager<ArticleData> beforeBase =
        queryService.findRecentArticlesWithCursor(
            null, null, null, new CursorPageParameter<>(base, 10, Direction.PREV), user);
    Assertions.assertEquals(1, beforeBase.getData().size());
    Assertions.assertEquals(article.getId(), beforeBase.getData().get(0).getId());

    CursorPager<ArticleData> fromOlder =
        queryService.findRecentArticlesWithCursor(
            null,
            null,
            null,
            new CursorPageParameter<>(base.minusMillis(1), 10, Direction.PREV),
            user);
    Assertions.assertEquals(3, fromOlder.getData().size());
    Assertions.assertTrue(
        fromOlder.getData().stream()
            .map(ArticleData::getId)
            .collect(java.util.stream.Collectors.toSet())
            .containsAll(Arrays.asList(first.getId(), second.getId(), article.getId())));
  }

  @Test
  public void should_return_empty_pager_for_cursor_query_without_matches() {
    CursorPager<ArticleData> byTag =
        queryService.findRecentArticlesWithCursor(
            "no-such-tag", null, null, new CursorPageParameter<>(null, 20, Direction.NEXT), user);
    Assertions.assertTrue(byTag.getData().isEmpty());
    Assertions.assertNull(byTag.getStartCursor());
    Assertions.assertFalse(byTag.hasNext());
    Assertions.assertFalse(byTag.hasPrevious());

    CursorPager<ArticleData> feed =
        queryService.findUserFeedWithCursor(
            user, new CursorPageParameter<>(null, 20, Direction.NEXT));
    Assertions.assertTrue(feed.getData().isEmpty());
    Assertions.assertNull(feed.getEndCursor());
  }

  @Test
  public void should_page_user_feed_with_cursor() {
    User followed = new User("followed@test.com", "followed", "123", "", "");
    userRepository.save(followed);
    userRepository.saveRelation(new FollowRelation(user.getId(), followed.getId()));
    Instant base = Instant.parse("2016-02-18T03:22:56.637Z");
    Article a1 = new Article("f1", "desc", "body", Arrays.asList("java"), followed.getId(), base);
    Article a2 =
        new Article(
            "f2", "desc", "body", Arrays.asList("java"), followed.getId(), base.plusMillis(1));
    articleRepository.save(a1);
    articleRepository.save(a2);

    CursorPager<ArticleData> firstPage =
        queryService.findUserFeedWithCursor(
            user, new CursorPageParameter<>(null, 1, Direction.NEXT));
    Assertions.assertEquals(1, firstPage.getData().size());
    Assertions.assertEquals(a2.getId(), firstPage.getData().get(0).getId());
    Assertions.assertTrue(firstPage.hasNext());
    Assertions.assertTrue(firstPage.getData().get(0).getProfileData().isFollowing());

    CursorPager<ArticleData> secondPage =
        queryService.findUserFeedWithCursor(
            user,
            new CursorPageParameter<>(
                DateTimeCursor.parse(firstPage.getEndCursor().toString()), 1, Direction.NEXT));
    Assertions.assertEquals(1, secondPage.getData().size());
    Assertions.assertEquals(a1.getId(), secondPage.getData().get(0).getId());
    Assertions.assertFalse(secondPage.hasNext());
  }

  private Article articleAt(String title, Instant createdAt) {
    Article created =
        new Article(title, "desc", "body", Arrays.asList("java"), user.getId(), createdAt);
    articleRepository.save(created);
    return created;
  }
}
