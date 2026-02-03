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
import java.util.Collections;
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
class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  private CommentMutation commentMutation;
  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    commentMutation = new CommentMutation(articleRepository, commentRepository, commentQueryService);
    user = new User("test@example.com", "testuser", "password123", "bio", "image.jpg");
    article =
        new Article(
            "Test Title", "Test Description", "Test Body", Collections.emptyList(), user.getId());
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void createComment_success() {
    setAuthenticatedUser();

    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    CommentData commentData =
        new CommentData(
            "comment-id",
            "Test comment body",
            article.getId(),
            DateTime.now(),
            DateTime.now(),
            profileData);

    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("test-title", "Test comment body");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(commentData, result.getLocalContext());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void createComment_notAuthenticated() {
    org.springframework.security.authentication.AnonymousAuthenticationToken auth =
        new org.springframework.security.authentication.AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            java.util.Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.createComment("test-title", "Test comment body"));
  }

  @Test
  void createComment_articleNotFound() {
    setAuthenticatedUser();

    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("nonexistent", "Test comment body"));
  }

  @Test
  void removeComment_success() {
    setAuthenticatedUser();

    Comment comment = new Comment("Test comment body", user.getId(), article.getId());

    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), any())).thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("test-title", "comment-id");

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(commentRepository).remove(eq(comment));
  }

  @Test
  void removeComment_notAuthenticated() {
    org.springframework.security.authentication.AnonymousAuthenticationToken auth =
        new org.springframework.security.authentication.AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            java.util.Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.removeComment("test-title", "comment-id"));
  }

  @Test
  void removeComment_articleNotFound() {
    setAuthenticatedUser();

    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("nonexistent", "comment-id"));
  }

  @Test
  void removeComment_commentNotFound() {
    setAuthenticatedUser();

    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), any())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("test-title", "comment-id"));
  }

  @Test
  void removeComment_notAuthorized() {
    User otherUser = new User("other@example.com", "otheruser", "password", "bio", "image");
    Article otherArticle =
        new Article(
            "Other Title", "Other Description", "Other Body", Collections.emptyList(), otherUser.getId());
    Comment comment = new Comment("Test comment body", otherUser.getId(), otherArticle.getId());

    setAuthenticatedUser();

    when(articleRepository.findBySlug(eq("other-title"))).thenReturn(Optional.of(otherArticle));
    when(commentRepository.findById(eq(otherArticle.getId()), any())).thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("other-title", "comment-id"));
  }
}
