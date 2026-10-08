package io.spring.infrastructure.mybatis;

import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleFavoriteCount;
import io.spring.application.data.CommentData;
import io.spring.application.data.UserData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.repository.MyBatisArticleFavoriteRepository;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({
  MyBatisArticleRepository.class,
  MyBatisUserRepository.class,
  MyBatisCommentRepository.class,
  MyBatisArticleFavoriteRepository.class
})
public class ReadServiceRoundTripTest extends DbTestBase {
  private static final Instant BASE = Instant.parse("2016-02-18T03:22:56.637Z");

  @Autowired private ArticleRepository articleRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private CommentRepository commentRepository;
  @Autowired private ArticleFavoriteRepository articleFavoriteRepository;

  @Autowired private ArticleReadService articleReadService;
  @Autowired private ArticleFavoritesReadService articleFavoritesReadService;
  @Autowired private CommentReadService commentReadService;
  @Autowired private UserReadService userReadService;
  @Autowired private UserRelationshipQueryService userRelationshipQueryService;

  private User author;
  private User reader;
  private Article article;

  @BeforeEach
  public void setUp() {
    author = new User("author@test.com", "author", "123", "author bio", "author.png");
    reader = new User("reader@test.com", "reader", "123", "", "");
    userRepository.save(author);
    userRepository.save(reader);
    article =
        new Article(
            "Round trip", "desc", "body", Arrays.asList("java", "spring"), author.getId(), BASE);
    articleRepository.save(article);
  }

  @Test
  public void should_read_article_with_tags_and_timestamps() {
    ArticleData data = articleReadService.findById(article.getId());

    Assertions.assertEquals(article.getSlug(), data.getSlug());
    Assertions.assertEquals(BASE, data.getCreatedAt());
    Assertions.assertEquals(BASE, data.getUpdatedAt());
    Assertions.assertEquals(Arrays.asList("java", "spring"), sorted(data.getTagList()));
    Assertions.assertEquals(author.getId(), data.getProfileData().getId());
    Assertions.assertEquals("author", data.getProfileData().getUsername());

    ArticleData bySlug = articleReadService.findBySlug(article.getSlug());
    Assertions.assertEquals(data.getId(), bySlug.getId());

    List<ArticleData> many =
        articleReadService.findArticles(Collections.singletonList(article.getId()));
    Assertions.assertEquals(1, many.size());
    Assertions.assertEquals(Arrays.asList("java", "spring"), sorted(many.get(0).getTagList()));
  }

  @Test
  public void should_count_favorites() {
    Assertions.assertEquals(0, articleFavoritesReadService.articleFavoriteCount(article.getId()));
    Assertions.assertFalse(
        articleFavoritesReadService.isUserFavorite(reader.getId(), article.getId()));

    articleFavoriteRepository.save(new ArticleFavorite(article.getId(), reader.getId()));
    articleFavoriteRepository.save(new ArticleFavorite(article.getId(), author.getId()));

    Assertions.assertEquals(2, articleFavoritesReadService.articleFavoriteCount(article.getId()));
    Assertions.assertTrue(
        articleFavoritesReadService.isUserFavorite(reader.getId(), article.getId()));

    List<ArticleFavoriteCount> counts =
        articleFavoritesReadService.articlesFavoriteCount(
            Collections.singletonList(article.getId()));
    Assertions.assertEquals(1, counts.size());
    Assertions.assertEquals(article.getId(), counts.get(0).getId());
    Assertions.assertEquals(2, counts.get(0).getCount());

    Set<String> favorites =
        articleFavoritesReadService.userFavorites(
            Collections.singletonList(article.getId()), reader);
    Assertions.assertEquals(Collections.singleton(article.getId()), favorites);

    articleFavoriteRepository.remove(new ArticleFavorite(article.getId(), reader.getId()));
    Assertions.assertEquals(1, articleFavoritesReadService.articleFavoriteCount(article.getId()));
  }

  @Test
  public void should_order_comments_by_created_at_for_cursor_queries() {
    Comment oldest = new Comment("oldest", reader.getId(), article.getId(), BASE.plusMillis(1));
    Comment middle = new Comment("middle", author.getId(), article.getId(), BASE.plusMillis(2));
    Comment newest = new Comment("newest", reader.getId(), article.getId(), BASE.plusMillis(3));
    commentRepository.save(middle);
    commentRepository.save(newest);
    commentRepository.save(oldest);

    List<CommentData> all = commentReadService.findByArticleId(article.getId());
    Assertions.assertEquals(3, all.size());
    CommentData middleData =
        all.stream().filter(c -> c.getId().equals(middle.getId())).findFirst().get();
    Assertions.assertEquals(BASE.plusMillis(2), middleData.getCreatedAt());
    Assertions.assertEquals("author", middleData.getProfileData().getUsername());

    List<CommentData> newestFirst =
        commentReadService.findByArticleIdWithCursor(
            article.getId(), new CursorPageParameter<>(null, 10, Direction.NEXT));
    Assertions.assertEquals(Arrays.asList("newest", "middle", "oldest"), bodies(newestFirst));

    List<CommentData> oldestFirst =
        commentReadService.findByArticleIdWithCursor(
            article.getId(), new CursorPageParameter<>(null, 10, Direction.PREV));
    Assertions.assertEquals(Arrays.asList("oldest", "middle", "newest"), bodies(oldestFirst));

    List<CommentData> afterNewest =
        commentReadService.findByArticleIdWithCursor(
            article.getId(), new CursorPageParameter<>(newest.getCreatedAt(), 10, Direction.NEXT));
    Assertions.assertEquals(Arrays.asList("middle", "oldest"), bodies(afterNewest));

    CommentData byId = commentReadService.findById(oldest.getId());
    Assertions.assertEquals(BASE.plusMillis(1), byId.getCreatedAt());
  }

  @Test
  public void should_read_follow_relationships() {
    Assertions.assertFalse(
        userRelationshipQueryService.isUserFollowing(reader.getId(), author.getId()));
    Assertions.assertTrue(userRelationshipQueryService.followedUsers(reader.getId()).isEmpty());

    userRepository.saveRelation(new FollowRelation(reader.getId(), author.getId()));

    Assertions.assertTrue(
        userRelationshipQueryService.isUserFollowing(reader.getId(), author.getId()));
    Assertions.assertFalse(
        userRelationshipQueryService.isUserFollowing(author.getId(), reader.getId()));
    Assertions.assertEquals(
        Collections.singletonList(author.getId()),
        userRelationshipQueryService.followedUsers(reader.getId()));
    Assertions.assertEquals(
        Collections.singleton(author.getId()),
        userRelationshipQueryService.followingAuthors(
            reader.getId(), Arrays.asList(author.getId(), reader.getId())));

    userRepository.removeRelation(new FollowRelation(reader.getId(), author.getId()));
    Assertions.assertFalse(
        userRelationshipQueryService.isUserFollowing(reader.getId(), author.getId()));
  }

  @Test
  public void should_read_user_data() {
    UserData byUsername = userReadService.findByUsername("author");
    Assertions.assertEquals(author.getId(), byUsername.getId());
    Assertions.assertEquals("author@test.com", byUsername.getEmail());
    Assertions.assertEquals("author bio", byUsername.getBio());
    Assertions.assertEquals("author.png", byUsername.getImage());

    UserData byId = userReadService.findById(reader.getId());
    Assertions.assertEquals("reader", byId.getUsername());
    Assertions.assertNull(userReadService.findByUsername("nobody"));
  }

  private static List<String> sorted(List<String> values) {
    return values.stream().sorted().collect(Collectors.toList());
  }

  private static List<String> bodies(List<CommentData> comments) {
    return comments.stream().map(CommentData::getBody).collect(Collectors.toList());
  }
}
