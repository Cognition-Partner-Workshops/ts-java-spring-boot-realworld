package io.spring.core.reaction;

import java.util.Optional;

public interface ArticleReactionRepository {
  void save(ArticleReaction articleReaction);

  Optional<ArticleReaction> find(String articleId, String userId);

  void remove(ArticleReaction reaction);

  int countByArticleIdAndType(String articleId, ReactionType reactionType);
}
