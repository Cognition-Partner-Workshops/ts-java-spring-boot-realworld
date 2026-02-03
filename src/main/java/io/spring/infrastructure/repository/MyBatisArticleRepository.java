package io.spring.infrastructure.repository;

import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.article.Tag;
import io.spring.infrastructure.mybatis.mapper.ArticleMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MyBatisArticleRepository implements ArticleRepository {
  private static final Logger log = LoggerFactory.getLogger(MyBatisArticleRepository.class);
  private ArticleMapper articleMapper;

  public MyBatisArticleRepository(ArticleMapper articleMapper) {
    this.articleMapper = articleMapper;
  }

  @Override
  @Transactional
  public void save(Article article) {
    if (articleMapper.findById(article.getId()) == null) {
      log.debug("Creating new article with id: {}", article.getId());
      createNew(article);
    } else {
      log.debug("Updating existing article with id: {}", article.getId());
      articleMapper.update(article);
    }
  }

  private void createNew(Article article) {
    log.debug("Processing {} tags for article: {}", article.getTags().size(), article.getId());
    for (Tag tag : article.getTags()) {
      Tag targetTag =
          Optional.ofNullable(articleMapper.findTag(tag.getName()))
              .orElseGet(
                  () -> {
                    log.debug("Creating new tag: {}", tag.getName());
                    articleMapper.insertTag(tag);
                    return tag;
                  });
      articleMapper.insertArticleTagRelation(article.getId(), targetTag.getId());
    }
    articleMapper.insert(article);
    log.debug("Article inserted successfully: {}", article.getId());
  }

  @Override
  public Optional<Article> findById(String id) {
    log.debug("Finding article by id: {}", id);
    return Optional.ofNullable(articleMapper.findById(id));
  }

  @Override
  public Optional<Article> findBySlug(String slug) {
    log.debug("Finding article by slug: {}", slug);
    return Optional.ofNullable(articleMapper.findBySlug(slug));
  }

  @Override
  public void remove(Article article) {
    log.debug("Removing article with id: {}", article.getId());
    articleMapper.delete(article.getId());
    log.debug("Article removed successfully: {}", article.getId());
  }
}
