package io.spring.api;

import io.spring.api.adapter.RestToGraphQLAdapter;
import io.spring.application.article.NewArticleParam;
import io.spring.application.data.ArticleDataList;
import io.spring.core.user.User;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles")
@AllArgsConstructor
public class ArticlesApi {
  private RestToGraphQLAdapter restToGraphQLAdapter;

  @PostMapping
  public ResponseEntity<Map<String, Object>> createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam, @AuthenticationPrincipal User user) {
    Map<String, Object> response =
        restToGraphQLAdapter.createArticle(
            newArticleParam.getTitle(),
            newArticleParam.getDescription(),
            newArticleParam.getBody(),
            newArticleParam.getTagList(),
            user);
    return ResponseEntity.ok(response);
  }

  @GetMapping(path = "feed")
  public ResponseEntity<ArticleDataList> getFeed(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @AuthenticationPrincipal User user) {
    ArticleDataList response = restToGraphQLAdapter.getFeed(user, offset, limit);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<ArticleDataList> getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "favorited", required = false) String favoritedBy,
      @RequestParam(value = "author", required = false) String author,
      @AuthenticationPrincipal User user) {
    ArticleDataList response =
        restToGraphQLAdapter.getArticles(tag, author, favoritedBy, offset, limit, user);
    return ResponseEntity.ok(response);
  }
}
