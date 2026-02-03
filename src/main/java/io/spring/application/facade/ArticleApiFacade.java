package io.spring.application.facade;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

/**
 * Unified API Facade for Article operations. This facade provides a single entry point for both
 * REST and GraphQL APIs, ensuring consistent business logic and reducing code duplication.
 */
@Service
@AllArgsConstructor
public class ArticleApiFacade {

  private final ArticleCommandService articleCommandService;
  private final ArticleQueryService articleQueryService;
  private final ArticleRepository articleRepository;
  private final ArticleFavoriteRepository articleFavoriteRepository;

  /**
   * Creates a new article.
   *
   * @param title article title
   * @param description article description
   * @param body article body
   * @param tagList list of tags
   * @param user the authenticated user (author)
   * @return ArticleData for the created article
   */
  public ArticleData createArticle(
      String title, String description, String body, List<String> tagList, User user) {
    NewArticleParam newArticleParam =
        NewArticleParam.builder()
            .title(title)
            .description(description)
            .body(body)
            .tagList(tagList == null ? Collections.emptyList() : tagList)
            .build();
    Article article = articleCommandService.createArticle(newArticleParam, user);
    return articleQueryService.findById(article.getId(), user).get();
  }

  /**
   * Gets the Article entity by slug (used for GraphQL local context).
   *
   * @param slug article slug
   * @return Article entity
   * @throws ResourceNotFoundException if article not found
   */
  public Article getArticleEntity(String slug) {
    return articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Gets article data by slug.
   *
   * @param slug article slug
   * @param user the current user (optional, for favorited status)
   * @return ArticleData
   * @throws ResourceNotFoundException if article not found
   */
  public ArticleData getArticleBySlug(String slug, User user) {
    return articleQueryService.findBySlug(slug, user).orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Gets article data by ID.
   *
   * @param id article ID
   * @param user the current user (optional, for favorited status)
   * @return ArticleData
   * @throws ResourceNotFoundException if article not found
   */
  public ArticleData getArticleById(String id, User user) {
    return articleQueryService.findById(id, user).orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Updates an article.
   *
   * @param slug article slug
   * @param title new title (optional)
   * @param description new description (optional)
   * @param body new body (optional)
   * @param user the authenticated user
   * @return ArticleData for the updated article
   * @throws ResourceNotFoundException if article not found
   * @throws NoAuthorizationException if user is not the author
   */
  public ArticleData updateArticle(
      String slug, String title, String description, String body, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    if (!AuthorizationService.canWriteArticle(user, article)) {
      throw new NoAuthorizationException();
    }
    UpdateArticleParam updateArticleParam = new UpdateArticleParam(title, body, description);
    Article updatedArticle = articleCommandService.updateArticle(article, updateArticleParam);
    return articleQueryService.findBySlug(updatedArticle.getSlug(), user).get();
  }

  /**
   * Deletes an article.
   *
   * @param slug article slug
   * @param user the authenticated user
   * @throws ResourceNotFoundException if article not found
   * @throws NoAuthorizationException if user is not the author
   */
  public void deleteArticle(String slug, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    if (!AuthorizationService.canWriteArticle(user, article)) {
      throw new NoAuthorizationException();
    }
    articleRepository.remove(article);
  }

  /**
   * Favorites an article.
   *
   * @param slug article slug
   * @param user the authenticated user
   * @return ArticleData with updated favorite status
   * @throws ResourceNotFoundException if article not found
   */
  public ArticleData favoriteArticle(String slug, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getId());
    articleFavoriteRepository.save(articleFavorite);
    return articleQueryService.findBySlug(slug, user).get();
  }

  /**
   * Unfavorites an article.
   *
   * @param slug article slug
   * @param user the authenticated user
   * @return ArticleData with updated favorite status
   * @throws ResourceNotFoundException if article not found
   */
  public ArticleData unfavoriteArticle(String slug, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    articleFavoriteRepository
        .find(article.getId(), user.getId())
        .ifPresent(favorite -> articleFavoriteRepository.remove(favorite));
    return articleQueryService.findBySlug(slug, user).get();
  }

  /**
   * Gets recent articles with offset-based pagination (for REST API).
   *
   * @param tag filter by tag (optional)
   * @param author filter by author (optional)
   * @param favoritedBy filter by user who favorited (optional)
   * @param offset pagination offset
   * @param limit pagination limit
   * @param user the current user (optional, for favorited status)
   * @return ArticleDataList with articles and total count
   */
  public ArticleDataList getRecentArticles(
      String tag, String author, String favoritedBy, int offset, int limit, User user) {
    return articleQueryService.findRecentArticles(
        tag, author, favoritedBy, new Page(offset, limit), user);
  }

  /**
   * Gets recent articles with cursor-based pagination (for GraphQL API).
   *
   * @param tag filter by tag (optional)
   * @param author filter by author (optional)
   * @param favoritedBy filter by user who favorited (optional)
   * @param page cursor page parameter
   * @param user the current user (optional, for favorited status)
   * @return CursorPager with articles
   */
  public CursorPager<ArticleData> getRecentArticlesWithCursor(
      String tag,
      String author,
      String favoritedBy,
      CursorPageParameter<DateTime> page,
      User user) {
    return articleQueryService.findRecentArticlesWithCursor(tag, author, favoritedBy, page, user);
  }

  /**
   * Gets user feed with offset-based pagination (for REST API).
   *
   * @param user the authenticated user
   * @param offset pagination offset
   * @param limit pagination limit
   * @return ArticleDataList with articles and total count
   */
  public ArticleDataList getUserFeed(User user, int offset, int limit) {
    return articleQueryService.findUserFeed(user, new Page(offset, limit));
  }

  /**
   * Gets user feed with cursor-based pagination (for GraphQL API).
   *
   * @param user the user whose feed to get
   * @param page cursor page parameter
   * @return CursorPager with articles
   */
  public CursorPager<ArticleData> getUserFeedWithCursor(
      User user, CursorPageParameter<DateTime> page) {
    return articleQueryService.findUserFeedWithCursor(user, page);
  }
}
