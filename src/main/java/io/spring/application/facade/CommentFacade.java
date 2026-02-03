package io.spring.application.facade;

import io.spring.application.data.CommentData;
import io.spring.core.user.User;
import java.util.List;

/**
 * Facade interface for comment-related operations. This provides a unified interface for both REST
 * and GraphQL APIs to handle comment CRUD operations.
 */
public interface CommentFacade {

  /**
   * Create a new comment on an article.
   *
   * @param slug the article slug
   * @param body the comment body
   * @param user the authenticated user creating the comment
   * @return the created comment data
   */
  CommentData createComment(String slug, String body, User user);

  /**
   * Get all comments for an article.
   *
   * @param slug the article slug
   * @param user the current user (can be null for anonymous access)
   * @return the list of comments
   */
  List<CommentData> getComments(String slug, User user);

  /**
   * Delete a comment.
   *
   * @param slug the article slug
   * @param commentId the comment ID
   * @param user the authenticated user
   */
  void deleteComment(String slug, String commentId, User user);
}
