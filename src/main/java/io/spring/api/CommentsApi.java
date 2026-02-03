package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {
  private static final Logger log = LoggerFactory.getLogger(CommentsApi.class);
  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    log.info("Creating comment on article: {} by user: {}", slug, user.getUsername());
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(() -> {
          log.warn("Article not found for comment creation with slug: {}", slug);
          return new ResourceNotFoundException();
        });
    Comment comment = new Comment(newCommentParam.getBody(), user.getId(), article.getId());
    commentRepository.save(comment);
    log.info("Comment created successfully with id: {}", comment.getId());
    return ResponseEntity.status(201)
        .body(commentResponse(commentQueryService.findById(comment.getId(), user).get()));
  }

  @GetMapping
  public ResponseEntity getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    log.debug("Fetching comments for article: {}", slug);
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(() -> {
          log.warn("Article not found for fetching comments with slug: {}", slug);
          return new ResourceNotFoundException();
        });
    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), user);
    log.debug("Found {} comments for article: {}", comments.size(), slug);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal User user) {
    log.info("Deleting comment: {} from article: {} by user: {}", commentId, slug, user.getUsername());
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(() -> {
          log.warn("Article not found for comment deletion with slug: {}", slug);
          return new ResourceNotFoundException();
        });
    return commentRepository
        .findById(article.getId(), commentId)
        .map(
            comment -> {
              if (!AuthorizationService.canWriteComment(user, article, comment)) {
                log.warn("User {} not authorized to delete comment: {}", user.getUsername(), commentId);
                throw new NoAuthorizationException();
              }
              commentRepository.remove(comment);
              log.info("Comment deleted successfully: {}", commentId);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(() -> {
          log.warn("Comment not found: {} for article: {}", commentId, slug);
          return new ResourceNotFoundException();
        });
  }

  private Map<String, Object> commentResponse(CommentData commentData) {
    return new HashMap<String, Object>() {
      {
        put("comment", commentData);
      }
    };
  }
}

@Getter
@NoArgsConstructor
@JsonRootName("comment")
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
