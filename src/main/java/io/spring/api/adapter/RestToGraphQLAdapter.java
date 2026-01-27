package io.spring.api.adapter;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.DateTimeCursor;
import io.spring.application.Page;
import io.spring.application.ProfileQueryService;
import io.spring.application.UserQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ArticleDataList;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.application.data.UserWithToken;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UpdateUserCommand;
import io.spring.application.user.UpdateUserParam;
import io.spring.core.article.Article;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.graphql.ArticleMutation;
import io.spring.graphql.CommentMutation;
import io.spring.graphql.RelationMutation;
import io.spring.graphql.UserMutation;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.ProfilePayload;
import io.spring.graphql.types.UpdateArticleInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RestToGraphQLAdapter {

  private UserMutation userMutation;
  private ArticleMutation articleMutation;
  private CommentMutation commentMutation;
  private RelationMutation relationMutation;
  private JwtService jwtService;
  private UserQueryService userQueryService;
  private ArticleQueryService articleQueryService;
  private CommentQueryService commentQueryService;
  private ProfileQueryService profileQueryService;

  public Map<String, Object> createUser(RegisterParam registerParam) {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email(registerParam.getEmail())
            .username(registerParam.getUsername())
            .password(registerParam.getPassword())
            .build();

    DataFetcherResult<UserResult> result = userMutation.createUser(input);
    User user = (User) result.getLocalContext();
    UserData userData = userQueryService.findById(user.getId()).get();
    return userResponse(new UserWithToken(userData, jwtService.toToken(user)));
  }

  public Map<String, Object> login(String email, String password) {
    DataFetcherResult<UserPayload> result = userMutation.login(password, email);
    User user = (User) result.getLocalContext();
    UserData userData = userQueryService.findById(user.getId()).get();
    return userResponse(new UserWithToken(userData, jwtService.toToken(user)));
  }

  public Map<String, Object> updateUser(User currentUser, UpdateUserParam updateUserParam, String token) {
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email(updateUserParam.getEmail())
            .username(updateUserParam.getUsername())
            .bio(updateUserParam.getBio())
            .password(updateUserParam.getPassword())
            .image(updateUserParam.getImage())
            .build();

    userMutation.updateUser(input);
    UserData userData = userQueryService.findById(currentUser.getId()).get();
    return userResponse(new UserWithToken(userData, token));
  }

  public Map<String, Object> getCurrentUser(User currentUser, String token) {
    UserData userData = userQueryService.findById(currentUser.getId()).get();
    return userResponse(new UserWithToken(userData, token));
  }

  public Map<String, Object> createArticle(
      String title, String description, String body, List<String> tagList, User user) {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title(title)
            .description(description)
            .body(body)
            .tagList(tagList)
            .build();

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);
    Article article = (Article) result.getLocalContext();
    ArticleData articleData = articleQueryService.findById(article.getId(), user).get();
    return articleResponse(articleData);
  }

  public Map<String, Object> getArticle(String slug, User user) {
    return articleQueryService
        .findBySlug(slug, user)
        .map(this::articleResponse)
        .orElseThrow(ResourceNotFoundException::new);
  }

  public Map<String, Object> updateArticle(String slug, String title, String body, String description, User user) {
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title(title)
            .body(body)
            .description(description)
            .build();

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle(slug, input);
    Article article = (Article) result.getLocalContext();
    ArticleData articleData = articleQueryService.findBySlug(article.getSlug(), user).get();
    return articleResponse(articleData);
  }

  public void deleteArticle(String slug) {
    DeletionStatus result = articleMutation.deleteArticle(slug);
    if (!result.getSuccess()) {
      throw new RuntimeException("Failed to delete article");
    }
  }

  public ArticleDataList getArticles(
      String tag, String author, String favoritedBy, int offset, int limit, User user) {
    return articleQueryService.findRecentArticles(tag, author, favoritedBy, new Page(offset, limit), user);
  }

  public ArticleDataList getFeed(User user, int offset, int limit) {
    return articleQueryService.findUserFeed(user, new Page(offset, limit));
  }

  public Map<String, Object> favoriteArticle(String slug, User user) {
    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle(slug);
    Article article = (Article) result.getLocalContext();
    ArticleData articleData = articleQueryService.findBySlug(article.getSlug(), user).get();
    return articleResponse(articleData);
  }

  public Map<String, Object> unfavoriteArticle(String slug, User user) {
    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle(slug);
    Article article = (Article) result.getLocalContext();
    ArticleData articleData = articleQueryService.findBySlug(article.getSlug(), user).get();
    return articleResponse(articleData);
  }

  public Map<String, Object> createComment(String slug, String body, User user) {
    DataFetcherResult<CommentPayload> result = commentMutation.createComment(slug, body);
    CommentData commentData = (CommentData) result.getLocalContext();
    return commentResponse(commentData);
  }

  public List<CommentData> getComments(String articleId, User user) {
    return commentQueryService.findByArticleId(articleId, user);
  }

  public void deleteComment(String slug, String commentId) {
    DeletionStatus result = commentMutation.removeComment(slug, commentId);
    if (!result.getSuccess()) {
      throw new RuntimeException("Failed to delete comment");
    }
  }

  public Map<String, Object> getProfile(String username, User user) {
    return profileQueryService
        .findByUsername(username, user)
        .map(this::profileResponse)
        .orElseThrow(ResourceNotFoundException::new);
  }

  public Map<String, Object> followUser(String username) {
    ProfilePayload result = relationMutation.follow(username);
    ProfileData profileData =
        profileQueryService
            .findByUsername(username, null)
            .orElseThrow(ResourceNotFoundException::new);
    profileData.setFollowing(true);
    return profileResponse(profileData);
  }

  public Map<String, Object> unfollowUser(String username) {
    ProfilePayload result = relationMutation.unfollow(username);
    ProfileData profileData =
        profileQueryService
            .findByUsername(username, null)
            .orElseThrow(ResourceNotFoundException::new);
    profileData.setFollowing(false);
    return profileResponse(profileData);
  }

  private Map<String, Object> userResponse(UserWithToken userWithToken) {
    return new HashMap<String, Object>() {
      {
        put("user", userWithToken);
      }
    };
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    return new HashMap<String, Object>() {
      {
        put("article", articleData);
      }
    };
  }

  private Map<String, Object> commentResponse(CommentData commentData) {
    return new HashMap<String, Object>() {
      {
        put("comment", commentData);
      }
    };
  }

  private Map<String, Object> profileResponse(ProfileData profileData) {
    return new HashMap<String, Object>() {
      {
        put("profile", profileData);
      }
    };
  }
}
