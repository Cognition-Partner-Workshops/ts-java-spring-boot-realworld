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
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;

  @Mock private DgsDataFetchingEnvironment dfe;

  private CommentDatafetcher commentDatafetcher;

  private User testUser;
  private CommentData testCommentData;
  private ArticleData testArticleData;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    testUser = new User("test@example.com", "testuser", "password", "bio", "image");
    profileData = new ProfileData(testUser.getId(), testUser.getUsername(), "bio", "image", false);
    testCommentData =
        new CommentData(
            "comment-id", "Test comment body", "article-id", DateTime.now(), DateTime.now(), profileData);
    testArticleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "description",
            "body",
            false,
            0,
            DateTime.now(),
            DateTime.now(),
            Arrays.asList("java"),
            profileData);
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null));
  }

  @Test
  void getComment_success() {
    when(dfe.getLocalContext()).thenReturn(testCommentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("comment-id", result.getData().getId());
    assertEquals("Test comment body", result.getData().getBody());
  }

  @Test
  void articleComments_withFirst() {
    authenticateUser();
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", testArticleData);

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(testCommentData), Direction.NEXT, true);

    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), eq(testUser), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void articleComments_withLast() {
    authenticateUser();
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", testArticleData);

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(testCommentData), Direction.PREV, true);

    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), eq(testUser), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 10, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void articleComments_withoutFirstOrLast() {
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void articleComments_emptyResult() {
    authenticateUser();
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", testArticleData);

    CursorPager<CommentData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.NEXT, false);

    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), eq(testUser), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData().getEdges().isEmpty());
  }
}
