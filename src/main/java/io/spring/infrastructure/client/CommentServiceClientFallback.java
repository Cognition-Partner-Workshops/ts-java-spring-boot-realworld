package io.spring.infrastructure.client;

import io.spring.shared.dto.CommentDTO;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for CommentServiceClient. This class provides default responses when the
 * Comment Service is unavailable, ensuring graceful degradation of the application.
 */
@Component
public class CommentServiceClientFallback implements CommentServiceClient {

  private static final Logger logger = LoggerFactory.getLogger(CommentServiceClientFallback.class);

  @Override
  public Optional<CommentDTO> getCommentById(String id) {
    logger.warn("Comment Service unavailable. Fallback triggered for getCommentById: {}", id);
    return Optional.empty();
  }

  @Override
  public List<CommentDTO> getCommentsByArticleId(String articleId) {
    logger.warn(
        "Comment Service unavailable. Fallback triggered for getCommentsByArticleId: {}",
        articleId);
    return Collections.emptyList();
  }

  @Override
  public List<CommentDTO> getCommentsByUserId(String userId) {
    logger.warn(
        "Comment Service unavailable. Fallback triggered for getCommentsByUserId: {}", userId);
    return Collections.emptyList();
  }

  @Override
  public int getCommentCountByArticleId(String articleId) {
    logger.warn(
        "Comment Service unavailable. Fallback triggered for getCommentCountByArticleId: {}",
        articleId);
    return 0;
  }
}
