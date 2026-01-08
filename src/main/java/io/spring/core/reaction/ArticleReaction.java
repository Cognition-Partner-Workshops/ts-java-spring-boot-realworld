package io.spring.core.reaction;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;

@NoArgsConstructor
@Getter
@EqualsAndHashCode(of = {"articleId", "userId"})
public class ArticleReaction {
  private String articleId;
  private String userId;
  private ReactionType reactionType;
  private DateTime createdAt;

  public ArticleReaction(String articleId, String userId, ReactionType reactionType) {
    this.articleId = articleId;
    this.userId = userId;
    this.reactionType = reactionType;
    this.createdAt = new DateTime();
  }

  public void updateReactionType(ReactionType reactionType) {
    this.reactionType = reactionType;
  }
}
