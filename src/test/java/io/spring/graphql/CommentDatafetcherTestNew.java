package io.spring.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import io.spring.application.CursorPager.Direction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class CommentDatafetcherTestNew {

  private CommentQueryService commentQueryService;
  private CommentDatafetcher commentDatafetcher;
  private User user;
  private DgsDataFetchingEnvironment dfe;
  private DataFetchingEnvironment dataFetchingEnvironment;

  @BeforeEach
  public void setUp() {
    commentQueryService = mock(CommentQueryService.class);
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    dfe = mock(DgsDataFetchingEnvironment.class);
    dataFetchingEnvironment = mock(DataFetchingEnvironment.class);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser(User user) {
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private CommentData createCommentData(String commentId) {
    ProfileData profileData = new ProfileData(user.getId(), user.getUsername(), 
        user.getBio(), user.getImage(), false);
    return new CommentData(commentId, "Comment body", "article-id", 
        DateTime.now(), DateTime.now(), profileData);
  }

  @Test
  public void should_throw_exception_when_get_comments_without_first_or_last() {
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    try {
      commentDatafetcher.articleComments(null, null, null, null, dfe);
      assertThat("Should have thrown exception", false);
    } catch (IllegalArgumentException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_get_article_comments_with_first_parameter() {
    setAuthenticatedUser(user);
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);
    
    ProfileData profileData = new ProfileData(user.getId(), user.getUsername(), 
        user.getBio(), user.getImage(), false);
    ArticleData articleData = new ArticleData("article-id", "test-slug", "Test Title", 
        "Description", "Body", false, 0, DateTime.now(), DateTime.now(), 
        Arrays.asList("tag1"), profileData);
    Map<String, ArticleData> localContext = new HashMap<>();
    localContext.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(localContext);
    
    CommentData commentData = createCommentData("comment-id");
    CursorPager<CommentData> cursorPager = new CursorPager<>(
        Arrays.asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any(CursorPageParameter.class)))
        .thenReturn(cursorPager);

    DataFetcherResult<CommentsConnection> result = commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_get_article_comments_with_last_parameter() {
    setAuthenticatedUser(user);
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);
    
    ProfileData profileData = new ProfileData(user.getId(), user.getUsername(), 
        user.getBio(), user.getImage(), false);
    ArticleData articleData = new ArticleData("article-id", "test-slug", "Test Title", 
        "Description", "Body", false, 0, DateTime.now(), DateTime.now(), 
        Arrays.asList("tag1"), profileData);
    Map<String, ArticleData> localContext = new HashMap<>();
    localContext.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(localContext);
    
    CommentData commentData = createCommentData("comment-id");
    CursorPager<CommentData> cursorPager = new CursorPager<>(
        Arrays.asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any(CursorPageParameter.class)))
        .thenReturn(cursorPager);

    DataFetcherResult<CommentsConnection> result = commentDatafetcher.articleComments(null, null, 10, null, dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
  }

  @Test
  public void should_get_comment_payload_comment() {
    CommentData commentData = createCommentData("comment-id");
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
    assertThat(result.getData().getId(), is("comment-id"));
  }
}
