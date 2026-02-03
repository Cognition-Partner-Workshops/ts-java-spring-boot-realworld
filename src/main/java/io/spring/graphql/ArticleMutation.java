package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.application.facade.ArticleFacade;
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
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    NewArticleParam newArticleParam =
        NewArticleParam.builder()
            .title(input.getTitle())
            .description(input.getDescription())
            .body(input.getBody())
            .tagList(input.getTagList() == null ? Collections.emptyList() : input.getTagList())
            .build();
    ArticleData articleData = articleFacade.createArticle(newArticleParam, user);
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(articleData)
        .build();
  }

  @DgsMutation(field = MUTATION.UpdateArticle)
  public DataFetcherResult<ArticlePayload> updateArticle(
      @InputArgument("slug") String slug, @InputArgument("changes") UpdateArticleInput params) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    ArticleData articleData =
        articleFacade.updateArticle(
            slug,
            new UpdateArticleParam(params.getTitle(), params.getBody(), params.getDescription()),
            user);
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(articleData)
        .build();
  }

  @DgsMutation(field = MUTATION.FavoriteArticle)
  public DataFetcherResult<ArticlePayload> favoriteArticle(@InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    ArticleData articleData = articleFacade.favoriteArticle(slug, user);
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(articleData)
        .build();
  }

  @DgsMutation(field = MUTATION.UnfavoriteArticle)
  public DataFetcherResult<ArticlePayload> unfavoriteArticle(@InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    ArticleData articleData = articleFacade.unfavoriteArticle(slug, user);
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(articleData)
        .build();
  }

  @DgsMutation(field = MUTATION.DeleteArticle)
  public DeletionStatus deleteArticle(@InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    articleFacade.deleteArticle(slug, user);
    return DeletionStatus.newBuilder().success(true).build();
  }
}
