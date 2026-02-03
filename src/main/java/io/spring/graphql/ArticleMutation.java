package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.application.data.ArticleData;
import io.spring.application.facade.ArticleApiFacade;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import lombok.AllArgsConstructor;

@DgsComponent
@AllArgsConstructor
public class ArticleMutation {

  private ArticleApiFacade articleApiFacade;

  @DgsMutation(field = MUTATION.CreateArticle)
  public DataFetcherResult<ArticlePayload> createArticle(
      @InputArgument("input") CreateArticleInput input) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    ArticleData articleData =
        articleApiFacade.createArticle(
            input.getTitle(),
            input.getDescription(),
            input.getBody(),
            input.getTagList(),
            user);
    Article article = articleApiFacade.getArticleEntity(articleData.getSlug());
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(article)
        .build();
  }

  @DgsMutation(field = MUTATION.UpdateArticle)
  public DataFetcherResult<ArticlePayload> updateArticle(
      @InputArgument("slug") String slug, @InputArgument("changes") UpdateArticleInput params) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    ArticleData articleData =
        articleApiFacade.updateArticle(
            slug, params.getTitle(), params.getDescription(), params.getBody(), user);
    Article article = articleApiFacade.getArticleEntity(articleData.getSlug());
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(article)
        .build();
  }

  @DgsMutation(field = MUTATION.FavoriteArticle)
  public DataFetcherResult<ArticlePayload> favoriteArticle(@InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    articleApiFacade.favoriteArticle(slug, user);
    Article article = articleApiFacade.getArticleEntity(slug);
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(article)
        .build();
  }

  @DgsMutation(field = MUTATION.UnfavoriteArticle)
  public DataFetcherResult<ArticlePayload> unfavoriteArticle(@InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    articleApiFacade.unfavoriteArticle(slug, user);
    Article article = articleApiFacade.getArticleEntity(slug);
    return DataFetcherResult.<ArticlePayload>newResult()
        .data(ArticlePayload.newBuilder().build())
        .localContext(article)
        .build();
  }

  @DgsMutation(field = MUTATION.DeleteArticle)
  public DeletionStatus deleteArticle(@InputArgument("slug") String slug) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    articleApiFacade.deleteArticle(slug, user);
    return DeletionStatus.newBuilder().success(true).build();
  }
}
