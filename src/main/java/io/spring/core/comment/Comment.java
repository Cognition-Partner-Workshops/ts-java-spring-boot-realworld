package io.spring.core.comment;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

/**
 * Domain entity representing a comment on an article.
 *
 * <p>Comments allow users to engage with articles by posting text responses. Each comment is
 * associated with a specific article and authored by a user. Comments track their creation
 * timestamp and are identified by a unique UUID.
 *
 * @see io.spring.core.article.Article
 * @see io.spring.core.user.User
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Comment {
  private String id;
  private String body;
  private String userId;
  private String articleId;
  private DateTime createdAt;

  public Comment(String body, String userId, String articleId) {
    this.id = UUID.randomUUID().toString();
    this.body = body;
    this.userId = userId;
    this.articleId = articleId;
    this.createdAt = new DateTime();
  }
}
