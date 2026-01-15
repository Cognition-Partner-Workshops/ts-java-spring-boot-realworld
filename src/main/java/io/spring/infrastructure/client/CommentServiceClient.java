package io.spring.infrastructure.client;

import io.spring.shared.dto.CommentDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for communicating with the Comment Service. This client will be used when the
 * application is decomposed into microservices to fetch comment information from the Comment
 * Service.
 *
 * <p>The fallback class provides resilient behavior when the Comment Service is unavailable.
 */
@FeignClient(
    name = "comment-service",
    url = "${comment-service.url:http://localhost:8083}",
    fallback = CommentServiceClientFallback.class)
public interface CommentServiceClient {

  @GetMapping("/api/comments/{id}")
  Optional<CommentDTO> getCommentById(@PathVariable("id") String id);

  @GetMapping("/api/comments/article/{articleId}")
  List<CommentDTO> getCommentsByArticleId(@PathVariable("articleId") String articleId);

  @GetMapping("/api/comments/user/{userId}")
  List<CommentDTO> getCommentsByUserId(@PathVariable("userId") String userId);

  @GetMapping("/api/comments/article/{articleId}/count")
  int getCommentCountByArticleId(@PathVariable("articleId") String articleId);
}
