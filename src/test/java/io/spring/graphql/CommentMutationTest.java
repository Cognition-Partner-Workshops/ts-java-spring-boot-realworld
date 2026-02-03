package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;

  @Mock private CommentRepository commentRepository;

  @Mock private CommentQueryService commentQueryService;

  private CommentMutation commentMutation;

  private User testUser;
  private Article testArticle;
  private Comment testComment;
  private CommentData testCommentData;

  @BeforeEach
  void setUp() {
    commentMutation = new CommentMutation(articleRepository, commentRepository, commentQueryService);
    testUser = new User("test@example.com", "testuser", "password", "bio", "image");
    testArticle =
        new Article("Test Title", "description", "body", Arrays.asList("java"), testUser.getId());
    testComment = new Comment("Test comment body", testUser.getId(), testArticle.getId());
    ProfileData profileData = new ProfileData(testUser.getId(), testUser.getUsername(), "bio", "image", false);
    testCommentData =
        new CommentData(
            testComment.getId(),
            testComment.getBody(),
            testComment.getArticleId(),
            DateTime.now(),
            DateTime.now(),
            profileData);
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null));
  }

  @Test
  void createComment_success() {
    authenticateUser();
    String slug = "test-title";
    String body = "Test comment body";

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(testArticle));
    when(commentQueryService.findById(any(), eq(testUser))).thenReturn(Optional.of(testCommentData));

    DataFetcherResult<CommentPayload> result = commentMutation.createComment(slug, body);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(testCommentData, result.getLocalContext());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void createComment_notAuthenticated() {
    String slug = "test-title";
    String body = "Test comment body";

    assertThrows(AuthenticationException.class, () -> commentMutation.createComment(slug, body));
  }

  @Test
  void createComment_articleNotFound() {
    authenticateUser();
    String slug = "nonexistent";
    String body = "Test comment body";

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> commentMutation.createComment(slug, body));
  }

  @Test
  void removeComment_success() {
    authenticateUser();
    String slug = "test-title";
    String commentId = testComment.getId();

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(testArticle));
    when(commentRepository.findById(eq(testArticle.getId()), eq(commentId)))
        .thenReturn(Optional.of(testComment));

    DeletionStatus result = commentMutation.removeComment(slug, commentId);

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(commentRepository).remove(eq(testComment));
  }

  @Test
  void removeComment_notAuthenticated() {
    String slug = "test-title";
    String commentId = "comment-id";

    assertThrows(
        AuthenticationException.class, () -> commentMutation.removeComment(slug, commentId));
  }

  @Test
  void removeComment_articleNotFound() {
    authenticateUser();
    String slug = "nonexistent";
    String commentId = "comment-id";

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.removeComment(slug, commentId));
  }

  @Test
  void removeComment_commentNotFound() {
    authenticateUser();
    String slug = "test-title";
    String commentId = "nonexistent-comment";

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(testArticle));
    when(commentRepository.findById(eq(testArticle.getId()), eq(commentId)))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.removeComment(slug, commentId));
  }

  @Test
  void removeComment_notAuthorized() {
    User anotherUser = new User("other@example.com", "other", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(anotherUser, null));

    String slug = "test-title";
    String commentId = testComment.getId();

    when(articleRepository.findBySlug(eq(slug))).thenReturn(Optional.of(testArticle));
    when(commentRepository.findById(eq(testArticle.getId()), eq(commentId)))
        .thenReturn(Optional.of(testComment));

    assertThrows(
        NoAuthorizationException.class, () -> commentMutation.removeComment(slug, commentId));
  }
}
