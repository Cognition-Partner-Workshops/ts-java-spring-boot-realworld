package io.spring.application.article;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@AllArgsConstructor
public class ArticleCommandService {

  private static final Logger log = LoggerFactory.getLogger(ArticleCommandService.class);

  private ArticleRepository articleRepository;

  public Article createArticle(@Valid NewArticleParam newArticleParam, User creator) {
    log.info("Entering createArticle() with title={}", newArticleParam.getTitle());
    Article article =
        new Article(
            newArticleParam.getTitle(),
            newArticleParam.getDescription(),
            newArticleParam.getBody(),
            newArticleParam.getTagList(),
            creator.getId());
    articleRepository.save(article);
    log.info("Exiting createArticle()");
    return article;
  }

  public Article updateArticle(Article article, @Valid UpdateArticleParam updateArticleParam) {
    log.info("Entering updateArticle() with id={}", article.getId());
    article.update(
        updateArticleParam.getTitle(),
        updateArticleParam.getDescription(),
        updateArticleParam.getBody());
    articleRepository.save(article);
    log.info("Exiting updateArticle()");
    return article;
  }
}
