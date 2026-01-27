package io.spring.api;

import io.spring.api.adapter.RestToGraphQLAdapter;
import io.spring.core.user.User;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "articles/{slug}/favorite")
@AllArgsConstructor
public class ArticleFavoriteApi {
  private RestToGraphQLAdapter restToGraphQLAdapter;

  @PostMapping
  public ResponseEntity<Map<String, Object>> favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Map<String, Object> response = restToGraphQLAdapter.favoriteArticle(slug, user);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping
  public ResponseEntity<Map<String, Object>> unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Map<String, Object> response = restToGraphQLAdapter.unfavoriteArticle(slug, user);
    return ResponseEntity.ok(response);
  }
}
