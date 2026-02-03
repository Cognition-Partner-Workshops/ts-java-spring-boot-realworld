package io.spring.application.facade;

import io.spring.application.Page;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.user.User;

/**
 * Facade interface for article-related operations. This provides a unified interface for both REST
 * and GraphQL APIs to handle article CRUD operations, favorites, and queries.
 */
public interface ArticleFacade {

  /**
   * Create a new article.
   *
   * @param newArticleParam the article creation parameters
   * @param user the authenticated user creating the article
   * @return the created article data
   */
  ArticleData createArticle(NewArticleParam newArticleParam, User user);

  /**
   * Get an article by its slug.
   *
   * @param slug the article slug
   * @param user the current user (can be null for anonymous access)
   * @return the article data
   */
  ArticleData getArticle(String slug, User user);

  /**
   * Update an existing article.
   *
   * @param slug the article slug
   * @param updateArticleParam the update parameters
   * @param user the authenticated user
   * @return the updated article data
   */
  ArticleData updateArticle(String slug, UpdateArticleParam updateArticleParam, User user);

  /**
   * Delete an article.
   *
   * @param slug the article slug
   * @param user the authenticated user
   */
  void deleteArticle(String slug, User user);

  /**
   * Get a list of articles with optional filters.
   *
   * @param tag filter by tag
   * @param author filter by author username
   * @param favoritedBy filter by user who favorited
   * @param page pagination parameters
   * @param user the current user (can be null for anonymous access)
   * @return the list of articles
   */
  ArticleDataList getArticles(String tag, String author, String favoritedBy, Page page, User user);

  /**
   * Get the feed of articles from followed users.
   *
   * @param user the authenticated user
   * @param page pagination parameters
   * @return the list of articles from followed users
   */
  ArticleDataList getFeed(User user, Page page);

  /**
   * Favorite an article.
   *
   * @param slug the article slug
   * @param user the authenticated user
   * @return the updated article data
   */
  ArticleData favoriteArticle(String slug, User user);

  /**
   * Unfavorite an article.
   *
   * @param slug the article slug
   * @param user the authenticated user
   * @return the updated article data
   */
  ArticleData unfavoriteArticle(String slug, User user);
}
