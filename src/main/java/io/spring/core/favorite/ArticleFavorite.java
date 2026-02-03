package io.spring.core.favorite;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Domain entity representing a user's favorite on an article.
 *
 * <p>This join entity captures the many-to-many relationship between users and articles they have
 * favorited. Favoriting an article allows users to bookmark content they find interesting and
 * contributes to the article's favorites count.
 *
 * @see io.spring.core.article.Article
 * @see io.spring.core.user.User
 */
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class ArticleFavorite {
  private String articleId;
  private String userId;

  public ArticleFavorite(String articleId, String userId) {
    this.articleId = articleId;
    this.userId = userId;
  }
}
