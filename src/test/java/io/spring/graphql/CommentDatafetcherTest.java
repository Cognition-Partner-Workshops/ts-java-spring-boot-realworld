package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;
  @Mock private DgsDataFetchingEnvironment dgsDataFetchingEnvironment;

  private CommentDatafetcher commentDatafetcher;
  private User user;
  private ProfileData profileData;
  private CommentData commentData;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@example.com", "testuser", "password123", "bio", "image.jpg");
    profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    commentData =
        new CommentData(
            "comment-id", "Test comment body", "article-id", DateTime.now(), DateTime.now(), profileData);
    articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "Test Description",
            "Test Body",
            false,
            0,
            DateTime.now(),
            DateTime.now(),
            Collections.emptyList(),
            profileData);
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void getComment_success() {
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("comment-id", result.getData().getId());
    assertEquals("Test comment body", result.getData().getBody());
  }

  @Test
  void articleComments_withFirst_success() {
    setAuthenticatedUser();

    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false);

    when(dgsDataFetchingEnvironment.getSource()).thenReturn(article);
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(commentQueryService.findByArticleIdWithCursor(
            eq(articleData.getId()), eq(user), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void articleComments_withLast_success() {
    setAuthenticatedUser();

    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), Direction.PREV, false);

    when(dgsDataFetchingEnvironment.getSource()).thenReturn(article);
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(commentQueryService.findByArticleIdWithCursor(
            eq(articleData.getId()), eq(user), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 10, null, dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void articleComments_withoutFirstOrLast_throwsException() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            commentDatafetcher.articleComments(null, null, null, null, dgsDataFetchingEnvironment));
  }

  @Test
  void articleComments_emptyComments() {
    setAuthenticatedUser();

    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    CursorPager<CommentData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    when(dgsDataFetchingEnvironment.getSource()).thenReturn(article);
    when(dgsDataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(commentQueryService.findByArticleIdWithCursor(
            eq(articleData.getId()), eq(user), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dgsDataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData().getEdges().isEmpty());
  }
}
