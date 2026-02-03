package io.spring.api;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}")
@AllArgsConstructor
public class ArticleApi {
  private static final Logger log = LoggerFactory.getLogger(ArticleApi.class);
  private ArticleQueryService articleQueryService;
  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;

  @GetMapping
  public ResponseEntity<?> article(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    log.debug("Fetching article with slug: {}", slug);
    return articleQueryService
        .findBySlug(slug, user)
        .map(articleData -> {
          log.debug("Article found: {}", articleData.getTitle());
          return ResponseEntity.ok(articleResponse(articleData));
        })
        .orElseThrow(() -> {
          log.warn("Article not found with slug: {}", slug);
          return new ResourceNotFoundException();
        });
  }

  @PutMapping
  public ResponseEntity<?> updateArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UpdateArticleParam updateArticleParam) {
    log.info("Updating article with slug: {} by user: {}", slug, user.getUsername());
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
                log.warn("User {} not authorized to update article: {}", user.getUsername(), slug);
                throw new NoAuthorizationException();
              }
              Article updatedArticle =
                  articleCommandService.updateArticle(article, updateArticleParam);
              log.info("Article updated successfully: {}", updatedArticle.getSlug());
              return ResponseEntity.ok(
                  articleResponse(
                      articleQueryService.findBySlug(updatedArticle.getSlug(), user).get()));
            })
        .orElseThrow(() -> {
          log.warn("Article not found for update with slug: {}", slug);
          return new ResourceNotFoundException();
        });
  }

  @DeleteMapping
  public ResponseEntity deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    log.info("Deleting article with slug: {} by user: {}", slug, user.getUsername());
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
                log.warn("User {} not authorized to delete article: {}", user.getUsername(), slug);
                throw new NoAuthorizationException();
              }
              articleRepository.remove(article);
              log.info("Article deleted successfully: {}", slug);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(() -> {
          log.warn("Article not found for deletion with slug: {}", slug);
          return new ResourceNotFoundException();
        });
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    return new HashMap<String, Object>() {
      {
        put("article", articleData);
      }
    };
  }
}
