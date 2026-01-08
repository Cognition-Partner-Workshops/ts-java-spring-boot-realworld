package io.spring.api;

import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import java.util.HashMap;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger log = LoggerFactory.getLogger(ArticlesApi.class);

  private ArticleCommandService articleCommandService;
  private ArticleQueryService articleQueryService;

  @PostMapping
  public ResponseEntity createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam, @AuthenticationPrincipal User user) {
    log.info("Entering createArticle() with title: {}", newArticleParam.getTitle());
    Article article = articleCommandService.createArticle(newArticleParam, user);
    ResponseEntity response =
        ResponseEntity.ok(
            new HashMap<String, Object>() {
              {
                put("article", articleQueryService.findById(article.getId(), user).get());
              }
            });
    log.info("Exiting createArticle()");
    return response;
  }

  @GetMapping(path = "feed")
  public ResponseEntity getFeed(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @AuthenticationPrincipal User user) {
    log.info("Entering getFeed() with offset: {}, limit: {}", offset, limit);
    ResponseEntity response =
        ResponseEntity.ok(articleQueryService.findUserFeed(user, new Page(offset, limit)));
    log.info("Exiting getFeed()");
    return response;
  }

  @GetMapping
  public ResponseEntity getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "favorited", required = false) String favoritedBy,
      @RequestParam(value = "author", required = false) String author,
      @AuthenticationPrincipal User user) {
    log.info(
        "Entering getArticles() with offset: {}, limit: {}, tag: {}, favoritedBy: {}, author: {}",
        offset,
        limit,
        tag,
        favoritedBy,
        author);
    ResponseEntity response =
        ResponseEntity.ok(
            articleQueryService.findRecentArticles(
                tag, author, favoritedBy, new Page(offset, limit), user));
    log.info("Exiting getArticles()");
    return response;
  }
}
