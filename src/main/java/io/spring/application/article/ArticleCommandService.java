package io.spring.application.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Service
@Validated
@AllArgsConstructor
public class ArticleCommandService {

  private ArticleRepository articleRepository;

  public Article createArticle(@Valid NewArticleParam newArticleParam, User creator) {
    log.info(
        "Entering createArticle with parameters: title={}, userId={}",
        newArticleParam.getTitle(),
        creator != null ? creator.getId() : "anonymous");
    Article article =
        new Article(
            newArticleParam.getTitle(),
            newArticleParam.getDescription(),
            newArticleParam.getBody(),
            newArticleParam.getTagList(),
            creator.getId());
    articleRepository.save(article);
    log.info("Exiting createArticle with result: articleId={}", article.getId());
    return article;
  }

  public Article updateArticle(Article article, @Valid UpdateArticleParam updateArticleParam) {
    log.info("Entering updateArticle with parameters: articleId={}", article.getId());
    article.update(
        updateArticleParam.getTitle(),
        updateArticleParam.getDescription(),
        updateArticleParam.getBody());
    articleRepository.save(article);
    log.info("Exiting updateArticle with result: articleId={}", article.getId());
    return article;
  }
}
