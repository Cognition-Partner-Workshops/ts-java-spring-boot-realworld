package io.spring.infrastructure.mybatis.mapper;

import io.spring.core.reaction.ArticleReaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleReactionMapper {
  ArticleReaction find(@Param("articleId") String articleId, @Param("userId") String userId);

  void insert(@Param("articleReaction") ArticleReaction articleReaction);

  void update(@Param("articleReaction") ArticleReaction articleReaction);

  void delete(@Param("reaction") ArticleReaction reaction);

  int countByArticleIdAndType(
      @Param("articleId") String articleId, @Param("reactionType") String reactionType);
}
