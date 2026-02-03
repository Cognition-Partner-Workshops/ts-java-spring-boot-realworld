package io.spring.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class CommentMutationTest {

  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;
  private CommentMutation commentMutation;
  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    articleRepository = mock(ArticleRepository.class);
    commentRepository = mock(CommentRepository.class);
    commentQueryService = mock(CommentQueryService.class);
    commentMutation = new CommentMutation(articleRepository, commentRepository, commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    article = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList(), user.getId());
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser(User user) {
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  public void should_create_comment_successfully() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    
    ProfileData profileData = new ProfileData(user.getId(), user.getUsername(), 
        user.getBio(), user.getImage(), false);
    CommentData commentData = new CommentData("comment-id", "Test comment body", 
        article.getId(), DateTime.now(), DateTime.now(), profileData);
    when(commentQueryService.findById(any(), any())).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result = commentMutation.createComment("test-slug", "Test comment body");

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  public void should_throw_exception_when_create_comment_without_auth() {
    try {
      commentMutation.createComment("test-slug", "Test comment body");
      assertThat("Should have thrown exception", false);
    } catch (AuthenticationException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_create_comment_on_nonexistent_article() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    try {
      commentMutation.createComment("nonexistent", "Test comment body");
      assertThat("Should have thrown exception", false);
    } catch (ResourceNotFoundException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_delete_comment_successfully() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    Comment comment = new Comment("Test comment body", user.getId(), article.getId());
    when(commentRepository.findById(article.getId(), "comment-id"))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("test-slug", "comment-id");

    assertThat(result, is(notNullValue()));
    assertThat(result.getSuccess(), is(true));
    verify(commentRepository).remove(comment);
  }

  @Test
  public void should_throw_exception_when_delete_comment_without_auth() {
    try {
      commentMutation.removeComment("test-slug", "comment-id");
      assertThat("Should have thrown exception", false);
    } catch (AuthenticationException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_delete_comment_on_nonexistent_article() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    try {
      commentMutation.removeComment("nonexistent", "comment-id");
      assertThat("Should have thrown exception", false);
    } catch (ResourceNotFoundException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_delete_nonexistent_comment() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "nonexistent"))
        .thenReturn(Optional.empty());

    try {
      commentMutation.removeComment("test-slug", "nonexistent");
      assertThat("Should have thrown exception", false);
    } catch (ResourceNotFoundException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_delete_comment_not_owned() {
    User otherUser = new User("other@test.com", "otheruser", "password", "bio", "image");
    setAuthenticatedUser(otherUser);
    
    Article otherArticle = new Article("Test Title", "Test Description", "Test Body", 
        Arrays.asList(), "another-user-id");
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(otherArticle));
    Comment comment = new Comment("Test comment body", user.getId(), otherArticle.getId());
    when(commentRepository.findById(otherArticle.getId(), "comment-id"))
        .thenReturn(Optional.of(comment));

    try {
      commentMutation.removeComment("test-slug", "comment-id");
      assertThat("Should have thrown exception", false);
    } catch (NoAuthorizationException e) {
      assertThat(e, is(notNullValue()));
    }
  }
}
