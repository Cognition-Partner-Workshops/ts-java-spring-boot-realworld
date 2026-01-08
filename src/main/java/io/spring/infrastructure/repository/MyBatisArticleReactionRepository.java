package io.spring.infrastructure.repository;

import io.spring.core.reaction.ArticleReaction;
import io.spring.core.reaction.ArticleReactionRepository;
import io.spring.core.reaction.ReactionType;
import io.spring.infrastructure.mybatis.mapper.ArticleReactionMapper;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MyBatisArticleReactionRepository implements ArticleReactionRepository {
  private ArticleReactionMapper mapper;

  @Autowired
  public MyBatisArticleReactionRepository(ArticleReactionMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void save(ArticleReaction articleReaction) {
    ArticleReaction existing =
        mapper.find(articleReaction.getArticleId(), articleReaction.getUserId());
    if (existing == null) {
      mapper.insert(articleReaction);
    } else {
      mapper.update(articleReaction);
    }
  }

  @Override
  public Optional<ArticleReaction> find(String articleId, String userId) {
    return Optional.ofNullable(mapper.find(articleId, userId));
  }

  @Override
  public void remove(ArticleReaction reaction) {
    mapper.delete(reaction);
  }

  @Override
  public int countByArticleIdAndType(String articleId, ReactionType reactionType) {
    return mapper.countByArticleIdAndType(articleId, reactionType.name());
  }
}
