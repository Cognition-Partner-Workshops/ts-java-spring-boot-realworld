package io.spring.infrastructure.mybatis;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.mybatis.readservice.ArticleReadService;
import io.spring.infrastructure.repository.MyBatisArticleRepository;
import io.spring.infrastructure.repository.MyBatisCommentRepository;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Import({
  MyBatisArticleRepository.class,
  MyBatisUserRepository.class,
  MyBatisCommentRepository.class
})
public class InstantHandlerDbRoundTripTest extends DbTestBase {
  private static final Instant INSTANT = Instant.parse("2016-02-18T03:22:56.637Z");

  @Autowired private ArticleRepository articleRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private CommentRepository commentRepository;
  @Autowired private ArticleReadService articleReadService;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  public void should_round_trip_article_timestamps_with_millisecond_precision() {
    User user = new User("ts@test.com", "ts", "123", "", "");
    userRepository.save(user);
    Article article =
        new Article("ts", "desc", "body", Arrays.asList("java"), user.getId(), INSTANT);
    articleRepository.save(article);

    Optional<Article> fetched = articleRepository.findById(article.getId());
    Assertions.assertTrue(fetched.isPresent());
    Assertions.assertEquals(INSTANT, fetched.get().getCreatedAt());
    Assertions.assertEquals(INSTANT, fetched.get().getUpdatedAt());
    Assertions.assertEquals(INSTANT, articleReadService.findById(article.getId()).getCreatedAt());

    Comment comment = new Comment("c", user.getId(), article.getId(), INSTANT.plusMillis(5));
    commentRepository.save(comment);
    Optional<Comment> fetchedComment = commentRepository.findById(article.getId(), comment.getId());
    Assertions.assertTrue(fetchedComment.isPresent());
    Assertions.assertEquals(INSTANT.plusMillis(5), fetchedComment.get().getCreatedAt());
  }

  @Test
  public void should_read_textual_utc_timestamps_written_by_seed_sql() {
    User user = new User("seed@test.com", "seed", "123", "", "");
    userRepository.save(user);
    jdbcTemplate.update(
        "insert into articles(id, slug, title, description, body, user_id, created_at, updated_at)"
            + " values (?, ?, ?, ?, ?, ?, ?, ?)",
        "seed-article",
        "seed-article",
        "Seed",
        "desc",
        "body",
        user.getId(),
        "2016-02-18 03:22:56",
        "2016-02-19 04:23:57");

    Optional<Article> fetched = articleRepository.findById("seed-article");
    Assertions.assertTrue(fetched.isPresent());
    Assertions.assertEquals(Instant.parse("2016-02-18T03:22:56Z"), fetched.get().getCreatedAt());
    Assertions.assertEquals(Instant.parse("2016-02-19T04:23:57Z"), fetched.get().getUpdatedAt());
    Assertions.assertEquals(
        0, fetched.get().getCreatedAt().atOffset(ZoneOffset.UTC).getOffset().getTotalSeconds());
  }
}
