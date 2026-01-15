package io.spring.infrastructure.client;

import io.spring.shared.dto.ArticleDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for communicating with the Article Service. This client will be used when the
 * application is decomposed into microservices to fetch article information from the Article
 * Service.
 *
 * <p>The fallback class provides resilient behavior when the Article Service is unavailable.
 */
@FeignClient(
    name = "article-service",
    url = "${article-service.url:http://localhost:8082}",
    fallback = ArticleServiceClientFallback.class)
public interface ArticleServiceClient {

  @GetMapping("/api/articles/{id}")
  Optional<ArticleDTO> getArticleById(@PathVariable("id") String id);

  @GetMapping("/api/articles/slug/{slug}")
  Optional<ArticleDTO> getArticleBySlug(@PathVariable("slug") String slug);

  @GetMapping("/api/articles/author/{authorId}")
  List<ArticleDTO> getArticlesByAuthor(
      @PathVariable("authorId") String authorId,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "offset", defaultValue = "0") int offset);

  @GetMapping("/api/articles/tag/{tag}")
  List<ArticleDTO> getArticlesByTag(
      @PathVariable("tag") String tag,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "offset", defaultValue = "0") int offset);

  @GetMapping("/api/articles/{articleId}/favorites/count")
  int getFavoritesCount(@PathVariable("articleId") String articleId);

  @GetMapping("/api/articles/{articleId}/is-favorited/{userId}")
  boolean isFavorited(
      @PathVariable("articleId") String articleId, @PathVariable("userId") String userId);

  @GetMapping("/api/tags")
  List<String> getAllTags();
}
