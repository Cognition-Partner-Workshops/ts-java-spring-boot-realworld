package io.spring.infrastructure.client;

import io.spring.shared.dto.ArticleDTO;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for ArticleServiceClient. This class provides default responses when the
 * Article Service is unavailable, ensuring graceful degradation of the application.
 */
@Component
public class ArticleServiceClientFallback implements ArticleServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(ArticleServiceClientFallback.class);

  @Override
  public Optional<ArticleDTO> getArticleById(String id) {
    logger.warn("Article Service unavailable. Fallback triggered for getArticleById: {}", id);
    return Optional.empty();
  }

  @Override
  public Optional<ArticleDTO> getArticleBySlug(String slug) {
    logger.warn("Article Service unavailable. Fallback triggered for getArticleBySlug: {}", slug);
    return Optional.empty();
  }

  @Override
  public List<ArticleDTO> getArticlesByAuthor(String authorId, int limit, int offset) {
    logger.warn(
        "Article Service unavailable. Fallback triggered for getArticlesByAuthor: {}", authorId);
    return Collections.emptyList();
  }

  @Override
  public List<ArticleDTO> getArticlesByTag(String tag, int limit, int offset) {
    logger.warn("Article Service unavailable. Fallback triggered for getArticlesByTag: {}", tag);
    return Collections.emptyList();
  }

  @Override
  public int getFavoritesCount(String articleId) {
    logger.warn(
        "Article Service unavailable. Fallback triggered for getFavoritesCount: {}", articleId);
    return 0;
  }

  @Override
  public boolean isFavorited(String articleId, String userId) {
    logger.warn(
        "Article Service unavailable. Fallback triggered for isFavorited: {} by {}",
        articleId,
        userId);
    return false;
  }

  @Override
  public List<String> getAllTags() {
    logger.warn("Article Service unavailable. Fallback triggered for getAllTags");
    return Collections.emptyList();
  }
}
