package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.facade.ArticleFacade;
import io.spring.core.article.Article;
import io.spring.core.service.AuthContext;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Collections;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class ArticleMutation {

  private ArticleFacade articleFacade;

  @DgsMutation(field = MUTATION.CreateArticle)
  public DataFetcherResult<ArticlePayload> createArticle(
      @InputArgument("input") CreateArticleInput input) {
    User user = AuthContext.getCurrentUser().orElseThrow(AuthenticationException::new);
    NewArticleParam newArticleParam =
        NewArticleParam.builder()
            .title(input.getTitle())
            .description(input.getDescription())
            .body(input.getBody())
            .tagList(input.getTagList() == null ? Collections.emptyList() : input.getTagList())
            .build();
    Article article = articleFacade.createArticleEntity(newArticleParam, user);
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(article)
        .build();
  }

  @DgsMutation(field = MUTATION.UpdateArticle)
  public DataFetcherResult<ArticlePayload> updateArticle(
      @InputArgument("slug") String slug, @InputArgument("changes") UpdateArticleInput params) {
    User user = AuthContext.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article =
        articleFacade.updateArticleEntity(
            slug,
            new UpdateArticleParam(params.getTitle(), params.getBody(), params.getDescription()),
            user);
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(article)
        .build();
  }

  @DgsMutation(field = MUTATION.FavoriteArticle)
  public DataFetcherResult<ArticlePayload> favoriteArticle(@InputArgument("slug") String slug) {
    User user = AuthContext.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article = articleFacade.favoriteArticleEntity(slug, user);
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(article)
        .build();
  }

  @DgsMutation(field = MUTATION.UnfavoriteArticle)
  public DataFetcherResult<ArticlePayload> unfavoriteArticle(@InputArgument("slug") String slug) {
    User user = AuthContext.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article = articleFacade.unfavoriteArticleEntity(slug, user);
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(article)
        .build();
  }

  @DgsMutation(field = MUTATION.DeleteArticle)
  public DeletionStatus deleteArticle(@InputArgument("slug") String slug) {
    User user = AuthContext.getCurrentUser().orElseThrow(AuthenticationException::new);
    articleFacade.deleteArticle(slug, user);
    return DeletionStatus.newBuilder().success(true).build();
  }
}
