package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.reaction.ArticleReaction;
import io.spring.core.reaction.ArticleReactionRepository;
import io.spring.core.reaction.ReactionType;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "articles/{slug}/reactions")
@AllArgsConstructor
public class ArticleReactionApi {
  private ArticleReactionRepository articleReactionRepository;
  private ArticleRepository articleRepository;

  @PostMapping
  public ResponseEntity<?> reactToArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody NewReactionParam newReactionParam) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    ArticleReaction articleReaction =
        new ArticleReaction(article.getId(), user.getId(), newReactionParam.getType());
    articleReactionRepository.save(articleReaction);
    return ResponseEntity.status(201).body(reactionResponse(article.getId()));
  }

  @GetMapping
  public ResponseEntity<?> getReactions(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    return ResponseEntity.ok(reactionResponse(article.getId()));
  }

  @DeleteMapping
  public ResponseEntity<?> removeReaction(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    articleReactionRepository
        .find(article.getId(), user.getId())
        .ifPresent(reaction -> articleReactionRepository.remove(reaction));
    return ResponseEntity.ok(reactionResponse(article.getId()));
  }

  private Map<String, Object> reactionResponse(String articleId) {
    int likes = articleReactionRepository.countByArticleIdAndType(articleId, ReactionType.LIKE);
    int dislikes =
        articleReactionRepository.countByArticleIdAndType(articleId, ReactionType.DISLIKE);
    return new HashMap<String, Object>() {
      {
        put(
            "reaction",
            new HashMap<String, Object>() {
              {
                put("likes", likes);
                put("dislikes", dislikes);
              }
            });
      }
    };
  }
}

@Getter
@NoArgsConstructor
@JsonRootName("reaction")
class NewReactionParam {
  @NotNull(message = "can't be null")
  private ReactionType type;
}
