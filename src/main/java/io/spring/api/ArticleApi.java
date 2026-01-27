package io.spring.api;

import io.spring.api.adapter.RestToGraphQLAdapter;
import io.spring.application.article.UpdateArticleParam;
import io.spring.core.user.User;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
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
  private RestToGraphQLAdapter restToGraphQLAdapter;

  @GetMapping
  public ResponseEntity<Map<String, Object>> article(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Map<String, Object> response = restToGraphQLAdapter.getArticle(slug, user);
    return ResponseEntity.ok(response);
  }

  @PutMapping
  public ResponseEntity<Map<String, Object>> updateArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UpdateArticleParam updateArticleParam) {
    Map<String, Object> response =
        restToGraphQLAdapter.updateArticle(
            slug,
            updateArticleParam.getTitle(),
            updateArticleParam.getBody(),
            updateArticleParam.getDescription(),
            user);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    restToGraphQLAdapter.deleteArticle(slug);
    return ResponseEntity.noContent().build();
  }
}
