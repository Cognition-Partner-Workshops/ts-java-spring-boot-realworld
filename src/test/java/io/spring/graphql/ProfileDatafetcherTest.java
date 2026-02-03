package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.schema.DataFetchingEnvironment;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ProfileQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.Profile;
import io.spring.graphql.types.ProfilePayload;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
class ProfileDatafetcherTest {

  @Mock private ProfileQueryService profileQueryService;
  @Mock private DataFetchingEnvironment dataFetchingEnvironment;

  private ProfileDatafetcher profileDatafetcher;
  private User user;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    profileDatafetcher = new ProfileDatafetcher(profileQueryService);
    user = new User("test@example.com", "testuser", "password123", "bio", "image.jpg");
    profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser() {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void getUserProfile_success() {
    setAuthenticatedUser();

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(user);
    when(profileQueryService.findByUsername(eq(user.getUsername()), eq(user)))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getUserProfile(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals(user.getUsername(), result.getUsername());
    assertEquals(user.getBio(), result.getBio());
    assertEquals(user.getImage(), result.getImage());
  }

  @Test
  void getAuthor_success() {
    setAuthenticatedUser();

    ArticleData articleData =
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

    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    Article article = Article.newBuilder().slug("test-slug").build();

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(dataFetchingEnvironment.getSource()).thenReturn(article);
    when(profileQueryService.findByUsername(eq(user.getUsername()), eq(user)))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals(user.getUsername(), result.getUsername());
  }

  @Test
  void getCommentAuthor_success() {
    setAuthenticatedUser();

    CommentData commentData =
        new CommentData(
            "comment-id", "Test body", "article-id", DateTime.now(), DateTime.now(), profileData);

    Map<String, CommentData> map = new HashMap<>();
    map.put("comment-id", commentData);

    Comment comment = Comment.newBuilder().id("comment-id").build();

    when(dataFetchingEnvironment.getLocalContext()).thenReturn(map);
    when(dataFetchingEnvironment.getSource()).thenReturn(comment);
    when(profileQueryService.findByUsername(eq(user.getUsername()), eq(user)))
        .thenReturn(Optional.of(profileData));

    Profile result = profileDatafetcher.getCommentAuthor(dataFetchingEnvironment);

    assertNotNull(result);
    assertEquals(user.getUsername(), result.getUsername());
  }

  @Test
  void queryProfile_success() {
    setAuthenticatedUser();

    when(dataFetchingEnvironment.getArgument("username")).thenReturn("testuser");
    when(profileQueryService.findByUsername(eq("testuser"), eq(user)))
        .thenReturn(Optional.of(profileData));

    ProfilePayload result =
        profileDatafetcher.queryProfile("testuser", dataFetchingEnvironment);

    assertNotNull(result);
    assertNotNull(result.getProfile());
    assertEquals("testuser", result.getProfile().getUsername());
  }

  @Test
  void queryProfile_notFound() {
    setAuthenticatedUser();

    when(dataFetchingEnvironment.getArgument("username")).thenReturn("nonexistent");
    when(profileQueryService.findByUsername(eq("nonexistent"), eq(user)))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> profileDatafetcher.queryProfile("nonexistent", dataFetchingEnvironment));
  }
}
