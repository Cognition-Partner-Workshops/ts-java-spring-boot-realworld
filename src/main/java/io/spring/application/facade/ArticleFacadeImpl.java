package io.spring.application.facade;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ArticleFacadeImpl implements ArticleFacade {

  private final ArticleCommandService articleCommandService;
  private final ArticleQueryService articleQueryService;
  private final ArticleRepository articleRepository;
  private final ArticleFavoriteRepository articleFavoriteRepository;

  @Override
  public ArticleData createArticle(NewArticleParam newArticleParam, User user) {
    Article article = articleCommandService.createArticle(newArticleParam, user);
    return articleQueryService
        .findById(article.getId(), user)
        .orElseThrow(() -> new RuntimeException("Article not found after creation"));
  }

  @Override
  public ArticleData getArticle(String slug, User user) {
    return articleQueryService.findBySlug(slug, user).orElseThrow(ResourceNotFoundException::new);
  }

  @Override
  public ArticleData updateArticle(String slug, UpdateArticleParam updateArticleParam, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    if (!AuthorizationService.canWriteArticle(user, article)) {
      throw new NoAuthorizationException();
    }

    Article updatedArticle = articleCommandService.updateArticle(article, updateArticleParam);
    return articleQueryService
        .findBySlug(updatedArticle.getSlug(), user)
        .orElseThrow(() -> new RuntimeException("Article not found after update"));
  }

  @Override
  public void deleteArticle(String slug, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    if (!AuthorizationService.canWriteArticle(user, article)) {
      throw new NoAuthorizationException();
    }

    articleRepository.remove(article);
  }

  @Override
  public ArticleDataList getArticles(
      String tag, String author, String favoritedBy, Page page, User user) {
    return articleQueryService.findRecentArticles(tag, author, favoritedBy, page, user);
  }

  @Override
  public ArticleDataList getFeed(User user, Page page) {
    return articleQueryService.findUserFeed(user, page);
  }

  @Override
  public ArticleData favoriteArticle(String slug, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getId());
    articleFavoriteRepository.save(articleFavorite);

    return articleQueryService
        .findBySlug(slug, user)
        .orElseThrow(() -> new RuntimeException("Article not found after favoriting"));
  }

  @Override
  public ArticleData unfavoriteArticle(String slug, User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    articleFavoriteRepository
        .find(article.getId(), user.getId())
        .ifPresent(articleFavoriteRepository::remove);

    return articleQueryService
        .findBySlug(slug, user)
        .orElseThrow(() -> new RuntimeException("Article not found after unfavoriting"));
  }
}
