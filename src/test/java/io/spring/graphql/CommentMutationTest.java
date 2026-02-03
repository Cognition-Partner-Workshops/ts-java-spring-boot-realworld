package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  private CommentMutation commentMutation;

  @BeforeEach
  public void setUp() {
    commentMutation = new CommentMutation(articleRepository, commentRepository, commentQueryService);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User user) {
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
            user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(authToken);
  }

  @Test
  public void should_create_comment_successfully() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    Article article = new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), user.getId());
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    CommentData commentData =
        new CommentData(
            "comment-id",
            "Test comment body",
            article.getId(),
            DateTime.now(),
            DateTime.now(),
            profileData);

    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("test-article", "Test comment body");

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  public void should_create_comment_with_different_body() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    authenticateUser(user);

    Article article = new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), user.getId());
    ProfileData profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    CommentData commentData =
        new CommentData(
            "comment-id",
            "Different comment",
            article.getId(),
            DateTime.now(),
            DateTime.now(),
            profileData);

    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("test-article", "Different comment");

    assertNotNull(result);
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  public void should_throw_resource_not_found_when_article_not_found_for_create() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("nonexistent", "Test comment body"));
  }

  @Test
  public void should_delete_comment_successfully() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    Article article = new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), user.getId());
    Comment comment = new Comment("Test comment body", user.getId(), article.getId());

    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "comment-id")).thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("test-article", "comment-id");

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  public void should_delete_comment_by_article_author() {
    User articleAuthor = new User("author@test.com", "author", "password", "", "");
    User commentAuthor = new User("commenter@test.com", "commenter", "password", "", "");
    authenticateUser(articleAuthor);

    Article article = new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), articleAuthor.getId());
    Comment comment = new Comment("Test comment body", commentAuthor.getId(), article.getId());

    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "comment-id")).thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("test-article", "comment-id");

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  public void should_throw_resource_not_found_when_article_not_found_for_delete() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("nonexistent", "comment-id"));
  }

  @Test
  public void should_throw_resource_not_found_when_comment_not_found_for_delete() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    authenticateUser(user);

    Article article = new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), user.getId());

    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("test-article", "nonexistent"));
  }

  @Test
  public void should_throw_no_authorization_when_user_cannot_delete_comment() {
    User user = new User("test@test.com", "testuser", "password", "", "");
    User otherUser = new User("other@test.com", "otheruser", "password", "", "");
    authenticateUser(user);

    Article article =
        new Article("Test Article", "Test Description", "Test Body", Collections.emptyList(), otherUser.getId());
    Comment comment = new Comment("Test comment body", otherUser.getId(), article.getId());

    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "comment-id")).thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("test-article", "comment-id"));
  }
}
