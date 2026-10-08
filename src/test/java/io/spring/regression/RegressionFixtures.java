package io.spring.regression;

import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.Arrays;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Centralises every timestamp construction used by the regression suite so the Joda -> java.time
 * migration only touches this file.
 */
public final class RegressionFixtures {
  private RegressionFixtures() {}

  public static DateTime now() {
    return new DateTime(DateTimeZone.UTC);
  }

  public static DateTime atMillis(long millis) {
    return new DateTime(millis, DateTimeZone.UTC);
  }

  public static long millisOf(DateTime dateTime) {
    return dateTime.getMillis();
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
    DateTime t = now();
    return new CommentData(id, "comment " + id, articleId, t, t, profile(author, false));
  }
}
