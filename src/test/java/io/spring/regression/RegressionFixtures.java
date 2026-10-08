package io.spring.regression;

import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.time.Instant;
import java.util.Arrays;

/**
 * Centralises every timestamp construction used by the regression suite so the Joda -> java.time
 * migration only touches this file.
 */
public final class RegressionFixtures {
  private RegressionFixtures() {}

  public static Instant now() {
    return Instant.now();
  }

  public static Instant atMillis(long millis) {
    return Instant.ofEpochMilli(millis);
  }

  public static long millisOf(Instant instant) {
    return instant.toEpochMilli();
  }

  public static User user(String seed) {
    return new User(seed + "@example.com", seed, "secret-" + seed, "bio " + seed, "img-" + seed);
  }

  public static Article article(String seed, User author) {
    return new Article(
        "Title " + seed, "Desc " + seed, "Body " + seed, Arrays.asList("t1", "t2"), author.getId());
  }

  public static ProfileData profile(User user, boolean following) {
    return new ProfileData(
        user.getId(), user.getUsername(), user.getBio(), user.getImage(), following);
  }

  public static ArticleData articleData(Article article, User author) {
    return new ArticleData(
        article.getId(),
        article.getSlug(),
        article.getTitle(),
        article.getDescription(),
        article.getBody(),
        false,
        0,
        article.getCreatedAt(),
        article.getUpdatedAt(),
        Arrays.asList("t1", "t2"),
        profile(author, false));
  }

  public static CommentData commentData(String id, String articleId, User author) {
    Instant t = now();
    return new CommentData(id, "comment " + id, articleId, t, t, profile(author, false));
  }
}
