package io.spring.application.facade;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

/**
 * Unified API Facade for Comment operations. This facade provides a single entry point for both
 * REST and GraphQL APIs, ensuring consistent business logic and reducing code duplication.
 */
@Service
@AllArgsConstructor
public class CommentApiFacade {

  private final ArticleRepository articleRepository;
  private final CommentRepository commentRepository;
  private final CommentQueryService commentQueryService;

  /**
   * Creates a new comment on an article.
   *
   * @param slug article slug
   * @param body comment body
   * @param user the authenticated user
   * @return CommentData for the created comment
   * @throws ResourceNotFoundException if article not found
   */
  public CommentData createComment(String slug, String body, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    Comment comment = new Comment(body, user.getId(), article.getId());
    commentRepository.save(comment);
    return commentQueryService.findById(comment.getId(), user).get();
  }

  /**
   * Gets all comments for an article.
   *
   * @param slug article slug
   * @param user the current user (optional, for following status)
   * @return list of CommentData
   * @throws ResourceNotFoundException if article not found
   */
  public List<CommentData> getCommentsByArticleSlug(String slug, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    return commentQueryService.findByArticleId(article.getId(), user);
  }

  /**
   * Gets comments for an article with cursor-based pagination (for GraphQL API).
   *
   * @param articleId article ID
   * @param user the current user (optional, for following status)
   * @param page cursor page parameter
   * @return CursorPager with comments
   */
  public CursorPager<CommentData> getCommentsByArticleIdWithCursor(
      String articleId, User user, CursorPageParameter<DateTime> page) {
    return commentQueryService.findByArticleIdWithCursor(articleId, user, page);
  }

  /**
   * Gets a comment by ID.
   *
   * @param commentId comment ID
   * @param user the current user (optional, for following status)
   * @return CommentData
   * @throws ResourceNotFoundException if comment not found
   */
  public CommentData getCommentById(String commentId, User user) {
    return commentQueryService.findById(commentId, user).orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Deletes a comment.
   *
   * @param slug article slug
   * @param commentId comment ID
   * @param user the authenticated user
   * @throws ResourceNotFoundException if article or comment not found
   * @throws NoAuthorizationException if user is not authorized to delete the comment
   */
  public void deleteComment(String slug, String commentId, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    Comment comment =
        commentRepository
            .findById(article.getId(), commentId)
            .orElseThrow(ResourceNotFoundException::new);
    if (!AuthorizationService.canWriteComment(user, article, comment)) {
      throw new NoAuthorizationException();
    }
    commentRepository.remove(comment);
  }
}
